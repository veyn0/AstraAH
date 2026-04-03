package dev.veyno.astraAH.ah;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.econ.EconomyProvider;
import dev.veyno.astraAH.entity.AHTransactionHistoryEntry;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.entity.PlayerPreferences;
import dev.veyno.astraAH.entity.PlayerPreferencesCategoryEntry;
import dev.veyno.astraAH.storage.history.AHTransactionHistoryStorageProvider;
import dev.veyno.astraAH.storage.listings.AHListingsStorageProvider;
import dev.veyno.astraAH.storage.preferences.AHPlayerPreferencesStorageProvider;
import dev.veyno.astraAH.util.IDLocks;
import dev.veyno.astraAH.util.PurchaseResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AuctionHouse {

    private final AstraAH plugin;
    private final AHListingsStorageProvider storage;
    private final AHPlayerPreferencesStorageProvider preferencesStorage;
    private final AHTransactionHistoryStorageProvider transactionHistoryStorage;
    private final EconomyProvider economy;

    private volatile Map<UUID, Listing> listings = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPreferences> preferences = new ConcurrentHashMap<>();
    private final Map<UUID, List<AHTransactionHistoryEntry>> transactionHistory = new ConcurrentHashMap<>();

    public AuctionHouse(
            AstraAH plugin,
            AHListingsStorageProvider storage,
            AHPlayerPreferencesStorageProvider preferencesStorage,
            AHTransactionHistoryStorageProvider transactionHistoryStorage,
            EconomyProvider economy
    ) {
        this.plugin = plugin;
        this.storage = storage;
        this.preferencesStorage = preferencesStorage;
        this.transactionHistoryStorage = transactionHistoryStorage;
        this.economy = economy;
        startCacheRefreshSchedule();
    }

    public List<Listing> getListings(){
        return listings.values().stream().toList();
    }

    public List<Listing> getListingsBlocking(UUID playerId){
        synchronized (IDLocks.getLock(playerId)){
            List<Listing> result = new ArrayList<>();
            for(Listing l : getListings()){
                if(l.playerId().equals(playerId)) result.add(l);
            }
            return result;
        }
    }

    public Listing getListing(UUID listingId){
        return listings.get(listingId);
    }

    public PlayerPreferences getPreferencesBlocking(UUID playerId) {
        synchronized (IDLocks.getLock(playerId)) {
            PlayerPreferences cachedPreferences = preferences.get(playerId);
            if (cachedPreferences != null) {
                return cachedPreferences;
            }
            return onPreferencesUpdateBlocking(playerId);
        }
    }

    public void setPreferencesBlocking(UUID playerId, PlayerPreferences playerPreferences) {
        synchronized (IDLocks.getLock(playerId)) {
            PlayerPreferences normalizedPreferences = normalizePreferences(playerId, playerPreferences);
            preferencesStorage.setPreferences(playerId, normalizedPreferences);
            preferences.put(playerId, normalizedPreferences);
        }
    }

    public void addCategoryBlocking(UUID playerId, PlayerPreferencesCategoryEntry categoryEntry) {
        if (categoryEntry == null) {
            return;
        }

        synchronized (IDLocks.getLock(playerId)) {
            preferencesStorage.addCategory(playerId, categoryEntry);
            PlayerPreferences updatedPreferences = preferencesStorage.getPreferences(playerId);
            preferences.put(playerId, updatedPreferences == null ? createDefaultPreferences(playerId) : updatedPreferences);
        }
    }

    public List<AHTransactionHistoryEntry> getTransactionHistoryBlocking(UUID playerId) {
        synchronized (IDLocks.getLock(playerId)) {
            List<AHTransactionHistoryEntry> cachedHistory = transactionHistory.get(playerId);
            if (cachedHistory != null) {
                return cachedHistory;
            }
            return onTransactionHistoryUpdateBlocking(playerId);
        }
    }

    public void addTransactionHistoryEntryBlocking(AHTransactionHistoryEntry entry) {
        if (entry == null) {
            return;
        }

        synchronized (IDLocks.getLock(entry.playerId())) {
            transactionHistoryStorage.addEntry(entry);
            List<AHTransactionHistoryEntry> updatedHistory = new ArrayList<>(transactionHistory.getOrDefault(entry.playerId(), List.of()));
            updatedHistory.add(entry);
            updatedHistory.sort(Comparator.comparing(AHTransactionHistoryEntry::timeStamp).reversed());
            transactionHistory.put(entry.playerId(), List.copyOf(updatedHistory));
        }
    }

    public boolean isListingAvailableBlocking(UUID listingId){
        synchronized (IDLocks.getLock(listingId)){
            return storage.getListing(listingId) != null;
        }
    }

    public PurchaseResult onAttemptPurchase(Player buyer, UUID listingId){
        Listing listing = onListingUpdateBlocking(listingId);
        synchronized (IDLocks.getLock(listingId)){
            if(listing == null || storage.getListing(listingId)==null) return PurchaseResult.UNAVAILABLE;
            if(!economy.withdraw(buyer.getUniqueId(), listing.price())) return PurchaseResult.MISSING_FUNDS;
            if(!economy.add(listing.playerId(), listing.price())) return PurchaseResult.ERROR;
            storage.removeListing(listingId);
            removeListingFromCache(listingId);
        }
        addTransactionHistoryEntryBlocking(new AHTransactionHistoryEntry(
                UUID.randomUUID(),
                listing.content().clone(),
                buyer.getUniqueId(),
                listing.playerId(),
                listing.price(),
                Instant.now()
        ));
        return PurchaseResult.SUCCESS;
    }

    public boolean onAttemptRemove(UUID listingId){
        synchronized (IDLocks.getLock(listingId)){
            if (storage.getListing(listingId) == null) {
                return false;
            }
            storage.removeListing(listingId);
            removeListingFromCache(listingId);
            return true;
        }
    }

    public Listing onListingUpdateBlocking(UUID listingId){
        synchronized (IDLocks.getLock(listingId)) {
            Listing result = storage.getListing(listingId);
            if (result == null){
                removeListingFromCache(listingId);
                return null;
            }
            listings.put(listingId, result);
            return result;
        }

        //TODO: check if the listing exists, if yes update it to the cache, if not, make sure to delete it from cache.

    }

    public PlayerPreferences onPreferencesUpdateBlocking(UUID playerId) {
        synchronized (IDLocks.getLock(playerId)) {
            PlayerPreferences result = preferencesStorage.getPreferences(playerId);
            if (result == null) {
                result = createDefaultPreferences(playerId);
            }
            preferences.put(playerId, result);
            return result;
        }
    }

    public List<AHTransactionHistoryEntry> onTransactionHistoryUpdateBlocking(UUID playerId) {
        synchronized (IDLocks.getLock(playerId)) {
            List<AHTransactionHistoryEntry> result = transactionHistoryStorage.getEntries(playerId);
            List<AHTransactionHistoryEntry> immutableResult = result == null ? List.of() : List.copyOf(result);
            transactionHistory.put(playerId, immutableResult);
            return immutableResult;
        }
    }

    private void startCacheRefreshSchedule(){
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            refreshCache();
        }, 1, 120, TimeUnit.SECONDS);
    }

    private synchronized void refreshCache(){
        try {
            refreshListingsCache();
            refreshPreferencesCache();
            refreshTransactionHistoryCache();
        } catch (Exception e){
            plugin.getLogger().severe("Failed to refresh AuctionHouse caches: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshListingsCache() {
        List<Listing> listingsRaw = storage.getListings();
        Map<UUID, Listing> refreshedListings = new ConcurrentHashMap<>();
        if (listingsRaw != null) {
            for (Listing listing : listingsRaw) {
                refreshedListings.put(listing.listingId(), listing);
            }
        }
        listings = refreshedListings;
    }

    private void refreshPreferencesCache() {
        for (UUID playerId : new ArrayList<>(preferences.keySet())) {
            PlayerPreferences refreshedPreferences = preferencesStorage.getPreferences(playerId);
            preferences.put(playerId, refreshedPreferences == null ? createDefaultPreferences(playerId) : refreshedPreferences);
        }
    }

    private void refreshTransactionHistoryCache() {
        for (UUID playerId : new ArrayList<>(transactionHistory.keySet())) {
            List<AHTransactionHistoryEntry> refreshedHistory = transactionHistoryStorage.getEntries(playerId);
            transactionHistory.put(playerId, refreshedHistory == null ? List.of() : List.copyOf(refreshedHistory));
        }
    }

    private void removeListingFromCache(UUID listingId) {
        listings.remove(listingId);
    }

    private PlayerPreferences createDefaultPreferences(UUID playerId) {
        return new PlayerPreferences(playerId);
    }

    private PlayerPreferences normalizePreferences(UUID playerId, PlayerPreferences playerPreferences) {
        if (playerPreferences == null) {
            return createDefaultPreferences(playerId);
        }
        return new PlayerPreferences(
                playerId,
                playerPreferences.categoryEntries(),
                playerPreferences.showCategories(),
                playerPreferences.showHistory()
        );
    }



}
