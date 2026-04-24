package dev.veyno.astraAH.util;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.config.categories.CategoryFilterConfiguration;
import dev.veyno.astraAH.app.ListingController;
import dev.veyno.astraAH.app.PlayerDataController;
import dev.veyno.astraAH.data.dto.Category;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.data.dto.Preferences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for generating demo data during development.
 * <p>
 * All methods go through the existing {@link ListingController} and
 * {@link PlayerDataController} so caches stay consistent and persistence
 * behaves identically to real user actions.
 * <p>
 * Typical usage in a debug command:
 * <pre>{@code
 * DemoDataGenerator.generateRandomListings(plugin, 50);
 * DemoDataGenerator.generateListingsForFakePlayers(plugin, 5, 10);
 * DemoDataGenerator.applyConfiguredCategoriesToOnlinePlayers(plugin);
 * }</pre>
 * This class is not thread-safe on its own but the underlying services are.
 */
public final class DemoDataGenerator {

    /** A small curated material pool used as listing content. Air, legacy and
     *  non-item materials are filtered at generation time. */
    private static final List<Material> DEMO_MATERIALS = Arrays.stream(Material.values())
            .filter(m -> !m.isLegacy())
            .filter(Material::isItem)
            .filter(m -> !m.isAir())
            .collect(Collectors.toUnmodifiableList());

    private static final String[] DEMO_CURRENCIES = { null, "coins", "gems" };

    /** Status 0 matches {@code STATUS_ACTIVE} in {@code YamlListingsRepository}. */
    private static final int STATUS_ACTIVE = 0;

    private DemoDataGenerator() {}

    // -------------------------------------------------------------------------
    // Listings
    // -------------------------------------------------------------------------

    /**
     * Creates {@code count} listings with random content and prices, each one
     * attributed to a random online- or offline player known to the server.
     * Falls back to fake sellers if no players are available.
     *
     * @param plugin the plugin instance
     * @param count  number of listings to create
     * @return the created listings (already persisted and cached)
     */
    public static List<Listing> generateRandomListings(AstraAH plugin, int count) {
        if (count <= 0) return List.of();

        List<UUID> sellerPool = collectKnownSellerIds();
        if (sellerPool.isEmpty()) {
            plugin.getLogger().info("[Demo] No known players found, using fake seller UUIDs.");
            return generateListingsForFakePlayers(plugin, 3, Math.max(1, count / 3));
        }

        Random rng = new Random();
        List<Listing> created = new ArrayList<>(count);
        ListingController controller = plugin.getListingController();

        for (int i = 0; i < count; i++) {
            UUID seller = sellerPool.get(rng.nextInt(sellerPool.size()));
            Listing listing = buildDemoListing(seller, rng);
            controller.postListing(listing);
            created.add(listing);
        }
        plugin.getLogger().info("[Demo] Created " + created.size() + " random listings.");
        return created;
    }

    /**
     * Creates listings attributed to {@code sellerCount} freshly generated
     * fake seller UUIDs. Useful for populating the auction house on a server
     * with no real player data yet.
     *
     * @param plugin             the plugin instance
     * @param sellerCount        number of distinct fake sellers
     * @param listingsPerSeller  listings generated per fake seller
     * @return the created listings (already persisted and cached)
     */
    public static List<Listing> generateListingsForFakePlayers(AstraAH plugin,
                                                               int sellerCount,
                                                               int listingsPerSeller) {
        if (sellerCount <= 0 || listingsPerSeller <= 0) return List.of();

        Random rng = new Random();
        List<Listing> created = new ArrayList<>(sellerCount * listingsPerSeller);
        ListingController controller = plugin.getListingController();

        for (int s = 0; s < sellerCount; s++) {
            UUID fakeSeller = UUID.randomUUID();
            for (int l = 0; l < listingsPerSeller; l++) {
                Listing listing = buildDemoListing(fakeSeller, rng);
                controller.postListing(listing);
                created.add(listing);
            }
        }
        plugin.getLogger().info("[Demo] Created " + created.size()
                + " listings for " + sellerCount + " fake sellers.");
        return created;
    }

    /**
     * Creates {@code count} listings attributed to the given seller.
     *
     * @param plugin   the plugin instance
     * @param sellerId the seller UUID to attribute the listings to
     * @param count    number of listings to create
     * @return the created listings (already persisted and cached)
     */
    public static List<Listing> generateListingsForPlayer(AstraAH plugin,
                                                          UUID sellerId,
                                                          int count) {
        if (sellerId == null) throw new IllegalArgumentException("sellerId == null");
        if (count <= 0) return List.of();

        Random rng = new Random();
        List<Listing> created = new ArrayList<>(count);
        ListingController controller = plugin.getListingController();

        for (int i = 0; i < count; i++) {
            Listing listing = buildDemoListing(sellerId, rng);
            controller.postListing(listing);
            created.add(listing);
        }
        plugin.getLogger().info("[Demo] Created " + created.size()
                + " listings for seller " + sellerId + ".");
        return created;
    }

    // -------------------------------------------------------------------------
    // Categories / Preferences
    // -------------------------------------------------------------------------

    /**
     * Writes the categories configured in {@code config.yml} (section
     * {@code categories}) into the preferences of every currently online
     * player, overwriting their existing list. Existing preference flags are
     * preserved.
     *
     * @param plugin the plugin instance
     * @return the number of players that were updated
     */
    public static int applyConfiguredCategoriesToOnlinePlayers(AstraAH plugin) {
        List<Category> categories = buildCategoriesFromConfig(plugin);
        if (categories.isEmpty()) {
            plugin.getLogger().warning("[Demo] No categories configured in config.yml.");
            return 0;
        }

        PlayerDataController pdc = plugin.getPlayerDataController();
        int updated = 0;
        for (var online : Bukkit.getOnlinePlayers()) {
            UUID id = online.getUniqueId();
            PlayerData data = pdc.getPlayerData(id);
            if (data == null) continue;
            Preferences newPrefs = data.getPreferences().withCategories(categories);
            pdc.getPlayerDataService().setPlayerData(data.withPreferences(newPrefs), false);
            updated++;
        }
        plugin.getLogger().info("[Demo] Applied " + categories.size()
                + " categories to " + updated + " online players.");
        return updated;
    }

    /**
     * Applies the categories configured in {@code config.yml} to a single
     * player. Overwrites their existing category list.
     *
     * @param plugin   the plugin instance
     * @param playerId the player to update
     * @return true when the player had data and was updated
     */
    public static boolean applyConfiguredCategoriesToPlayer(AstraAH plugin, UUID playerId) {
        if (playerId == null) throw new IllegalArgumentException("playerId == null");
        List<Category> categories = buildCategoriesFromConfig(plugin);
        if (categories.isEmpty()) return false;

        PlayerDataController pdc = plugin.getPlayerDataController();
        PlayerData data = pdc.getPlayerData(playerId);
        if (data == null) return false;

        Preferences newPrefs = data.getPreferences().withCategories(categories);
        pdc.getPlayerDataService().setPlayerData(data.withPreferences(newPrefs), false);
        return true;
    }

    /**
     * Builds a list of {@link Category} instances mirroring the filters
     * defined in {@code config.yml} under {@code categories}. Useful when you
     * want to hand demo categories to a player without mutating persisted
     * data yourself.
     *
     * @param plugin the plugin instance
     * @return a freshly built list of categories, possibly empty
     */
    public static List<Category> buildCategoriesFromConfig(AstraAH plugin) {
        List<CategoryFilterConfiguration> filters = plugin.getConfiguration()
                .getConfiguredCategories()
                .getFilters();

        List<Category> result = new ArrayList<>(filters.size());
        for (CategoryFilterConfiguration f : filters) {
            Category c = new Category();
            c.setFilter(new ArrayList<>(f.getRules()));
            c.setPreview(f.getPreviewItem());
            result.add(c);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private static Listing buildDemoListing(UUID sellerId, Random rng) {
        ItemStack content = randomItem(rng);
        double price = roundTo2Decimals(1 + rng.nextDouble() * 9999);
        String currency = DEMO_CURRENCIES[rng.nextInt(DEMO_CURRENCIES.length)];
        long createdAt = System.currentTimeMillis()
                - (long) (rng.nextDouble() * 1000L * 60 * 60 * 24 * 7); // up to 7 days back

        return new Listing(
                UUID.randomUUID(),
                sellerId,
                content,
                createdAt,
                price,
                currency,
                STATUS_ACTIVE
        );
    }

    private static ItemStack randomItem(Random rng) {
        Material material = DEMO_MATERIALS.get(rng.nextInt(DEMO_MATERIALS.size()));
        int max = Math.max(1, material.getMaxStackSize());
        int amount = 1 + rng.nextInt(max);

        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Demo " + prettifyMaterial(material))
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Generated demo item")
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static String prettifyMaterial(Material m) {
        String s = m.name().toLowerCase().replace('_', ' ');
        StringBuilder sb = new StringBuilder(s.length());
        boolean capNext = true;
        for (char c : s.toCharArray()) {
            if (c == ' ') { sb.append(c); capNext = true; continue; }
            sb.append(capNext ? Character.toUpperCase(c) : c);
            capNext = false;
        }
        return sb.toString();
    }

    private static double roundTo2Decimals(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /**
     * Collects UUIDs of all online plus known offline players as a pool of
     * potential sellers. Deduplicated, order-preserving.
     */
    private static List<UUID> collectKnownSellerIds() {
        List<UUID> ids = new ArrayList<>();
        for (var online : Bukkit.getOnlinePlayers()) {
            ids.add(online.getUniqueId());
        }
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            UUID id = offline.getUniqueId();
            if (id != null && !ids.contains(id)) ids.add(id);
        }
        return ids;
    }
}