package dev.veyno.astraAH.data.repository.listings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.YamlStorage;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.data.repository.ListingsRepository;
import dev.veyno.astraAH.data.serialization.ListingBase64Serializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * YAML-backed {@link ListingsRepository} that stores all listings in a single
 * {@code listings.yml} file. Each listing is keyed by its UUID (as a String)
 * under the {@code listings} section, with the value being its Base64-encoded
 * binary representation (see {@link ListingBase64Serializer}).
 * <p>
 * Unlike the per-player YAML files, this repository holds a single long-lived
 * {@link YamlStorage} instance because all listings share one file and
 * reopening it on every access would be expensive. Concurrency is guarded by
 * the storage's intrinsic per-filename lock — this is the only serialization
 * point needed because atomicity is always scoped to a single file operation.
 * <p>
 * The file-level lock also provides the correctness guarantee that makes
 * {@link #removeIfPresent(UUID)} usable as the authoritative sold-out check:
 * two concurrent calls for the same listing ID will be serialized, so only
 * one can observe the entry and return {@code true}.
 */
public class YamlListingsRepository implements ListingsRepository {

    private static final String FILE_NAME = "listings";
    private static final String LISTINGS_SECTION = "listings";

    /** {@link Listing#getStatus()} value that represents an active listing. */
    private static final int STATUS_ACTIVE = 0;

    private final AstraAH plugin;
    private final YamlStorage storage;

    public YamlListingsRepository(AstraAH plugin, int saveIntervalSeconds) {
        this.plugin = plugin;
        this.storage = new YamlStorage(plugin, FILE_NAME, false, saveIntervalSeconds);
    }

    // -------------------------------------------------------------------------
    // Writes
    // -------------------------------------------------------------------------

    @Override
    public void createListing(Listing listing) {
        if (listing == null) throw new IllegalArgumentException("listing == null");
        if (listing.getListingId() == null) throw new IllegalArgumentException("listingId == null");

        synchronized (storage.getLock()) {
            String key = keyFor(listing.getListingId());
            if (storage.getFileConfiguration().contains(key)) {
                throw new IllegalStateException("Listing already exists: " + listing.getListingId());
            }
            storage.getFileConfiguration().set(key, ListingBase64Serializer.toBase64(listing));
            storage.saveFile();
        }
    }

    @Override
    public boolean removeIfPresent(UUID listingId) {
        if (listingId == null) throw new IllegalArgumentException("listingId == null");

        synchronized (storage.getLock()) {
            FileConfiguration cfg = storage.getFileConfiguration();
            String key = keyFor(listingId);
            if (!cfg.contains(key)) {
                return false;
            }
            cfg.set(key, null);
            storage.saveFile();
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // Reads
    // -------------------------------------------------------------------------

    @Override
    public Listing getListing(UUID listingId) {
        if (listingId == null) throw new IllegalArgumentException("listingId == null");

        synchronized (storage.getLock()) {
            String encoded = storage.getFileConfiguration().getString(keyFor(listingId));
            if (encoded == null || encoded.isEmpty()) return null;
            return ListingBase64Serializer.fromBase64(encoded);
        }
    }

    @Override
    public List<Listing> getActiveListings() {
        return collectListings(l -> l.getStatus() == STATUS_ACTIVE);
    }

    @Override
    public List<Listing> getInactiveListings() {
        return collectListings(l -> l.getStatus() != STATUS_ACTIVE);
    }

    @Override
    public List<Listing> getAllListings() {
        return collectListings(l -> true);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<Listing> collectListings(ListingFilter filter) {
        synchronized (storage.getLock()) {
            ConfigurationSection section = storage.getFileConfiguration().getConfigurationSection(LISTINGS_SECTION);
            if (section == null) return Collections.emptyList();

            List<Listing> result = new ArrayList<>();
            for (String idKey : section.getKeys(false)) {
                String encoded = section.getString(idKey);
                if (encoded == null || encoded.isEmpty()) continue;
                try {
                    Listing listing = ListingBase64Serializer.fromBase64(encoded);
                    if (filter.accept(listing)) result.add(listing);
                } catch (RuntimeException e) {
                    plugin.getLogger().warning(
                            "Failed to deserialize listing " + idKey + ": " + e.getMessage());
                }
            }
            return result;
        }
    }

    private static String keyFor(UUID listingId) {
        return LISTINGS_SECTION + "." + listingId;
    }

    @FunctionalInterface
    private interface ListingFilter {
        boolean accept(Listing listing);
    }
}