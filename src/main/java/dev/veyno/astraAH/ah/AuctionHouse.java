package dev.veyno.astraAH.ah;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.dto.ActionState;
import dev.veyno.astraAH.dto.AllowedPlayerActions;
import dev.veyno.astraAH.dto.PreferencesPlayerActions;
import dev.veyno.astraAH.econ.EconomyProvider;
import dev.veyno.astraAH.entity.*;
import dev.veyno.astraAH.dto.MainPageButtonLayout;
import dev.veyno.astraAH.dto.MainPageLayoutState;
import dev.veyno.astraAH.storage.actions.AHPlayerActionsStorageProvider;
import dev.veyno.astraAH.storage.history.AHTransactionHistoryStorageProvider;
import dev.veyno.astraAH.storage.listings.AHListingsStorageProvider;
import dev.veyno.astraAH.storage.preferences.AHPlayerPreferencesStorageProvider;
import dev.veyno.astraAH.ui.SortType;
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
    private final AHPlayerActionsStorageProvider playerActionsStorage;
    private final EconomyProvider economy;

    private volatile Map<UUID, Listing> listings = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPreferences> preferences = new ConcurrentHashMap<>();
    private final Map<UUID, List<AHTransactionHistoryEntry>> transactionHistory = new ConcurrentHashMap<>();
    private final Map<UUID, AllowedPlayerActions> allowedPlayerActions = new ConcurrentHashMap<>();

    public AuctionHouse(
            AstraAH plugin,
            AHListingsStorageProvider storage,
            AHPlayerPreferencesStorageProvider preferencesStorage,
            AHTransactionHistoryStorageProvider transactionHistoryStorage,
            AHPlayerActionsStorageProvider playerActionsStorage,
            EconomyProvider economy
    ) {
        this.plugin = plugin;
        this.storage = storage;
        this.preferencesStorage = preferencesStorage;
        this.transactionHistoryStorage = transactionHistoryStorage;
        this.playerActionsStorage = playerActionsStorage;
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

    public AllowedPlayerActions getAllowedPlayerActionsBlocking(UUID playerId) {
        synchronized (IDLocks.getLock(playerId)) {
            AllowedPlayerActions cachedActions = allowedPlayerActions.get(playerId);
            if (cachedActions != null) {
                return cachedActions;
            }
            return onAllowedPlayerActionsUpdateBlocking(playerId);
        }
    }

    public void setAllowedPlayerActionsBlocking(UUID playerId, AllowedPlayerActions playerActions) {
        synchronized (IDLocks.getLock(playerId)) {
            AllowedPlayerActions normalizedActions = normalizeAllowedPlayerActions(playerId, playerActions);
            playerActionsStorage.saveAllowedActions(normalizedActions);
            allowedPlayerActions.put(playerId, normalizedActions);
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

    public boolean createListingBlocking(Listing l){
        synchronized (IDLocks.getLock(l.listingId())){
            try {
                storage.saveListing(l);
            }catch (Exception e){
                return false;
            }
        }
        onListingUpdateBlocking(l.listingId());
        return true;
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

    public AllowedPlayerActions onAllowedPlayerActionsUpdateBlocking(UUID playerId) {
        synchronized (IDLocks.getLock(playerId)) {
            AllowedPlayerActions result = playerActionsStorage.getAllowedActions(playerId);
            if (result == null) {
                result = createDefaultAllowedPlayerActions(playerId);
            }
            allowedPlayerActions.put(playerId, result);
            return result;
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
            refreshAllowedPlayerActionsCache();
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

    private void refreshAllowedPlayerActionsCache() {
        for (UUID playerId : new ArrayList<>(allowedPlayerActions.keySet())) {
            AllowedPlayerActions refreshedActions = playerActionsStorage.getAllowedActions(playerId);
            allowedPlayerActions.put(playerId, refreshedActions == null ? createDefaultAllowedPlayerActions(playerId) : refreshedActions);
        }
    }

    private void removeListingFromCache(UUID listingId) {
        listings.remove(listingId);
    }

    private PlayerPreferences createDefaultPreferences(UUID playerId) {
        return new PlayerPreferences(playerId);
    }

    private AllowedPlayerActions createDefaultAllowedPlayerActions(UUID playerId) {
        return new AllowedPlayerActions(playerId);
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

    private AllowedPlayerActions normalizeAllowedPlayerActions(UUID playerId, AllowedPlayerActions playerActions) {
        if (playerActions == null) {
            return createDefaultAllowedPlayerActions(playerId);
        }

        return new AllowedPlayerActions(
                playerId,
                normalizePreferencesPlayerActions(playerActions.getPreferencesPlayerActions()),
                playerActions.getCategories(),
                playerActions.getSettings(),
                playerActions.getMyListings(),
                playerActions.getRefresh(),
                playerActions.getSort(),
                playerActions.getSearch(),
                playerActions.getHistory()
        );
    }

    private PreferencesPlayerActions normalizePreferencesPlayerActions(PreferencesPlayerActions playerActions) {
        if (playerActions == null) {
            return new PreferencesPlayerActions();
        }

        return new PreferencesPlayerActions(
                playerActions.getShowAdvancedCategories(),
                playerActions.getShowAdvancedHistory(),
                playerActions.getReloadOnOpen(),
                playerActions.getDefaultFilter(),
                playerActions.getDefaultSort()
        );
    }

    public MainPageLayoutState getLayout(Player p){


        return null;
    }

    public MainPageLayoutState getLayoutBlocking(Player p){
        PlayerPreferences playerPreferences = getPreferencesBlocking(p.getUniqueId());
        AllowedPlayerActions allowedActions = getAllowedPlayerActionsBlocking(p.getUniqueId());

        MainPageLayoutState.ButtonLayout categories = getCategoryLayout(p, playerPreferences, allowedActions);

        MainPageLayoutState.ButtonLayout history = getHistoryLayout(p, playerPreferences, allowedActions);

        SortType sortType = SortType.NAME_A_Z; //TODO: add configurable default sortType for individual palyers

        boolean showSettings = isAllowedSettings(p, allowedActions);

        boolean showMyListings = isAllowedMyListings(p, allowedActions);

        boolean showRefresh = isAllowedRefresh(p, allowedActions);

        boolean showSort = isAllowedSort(p, allowedActions);

        boolean showSearch = isAllowedSearch(p, allowedActions);

        MainPageButtonLayout buttonLayout = new MainPageButtonLayout(showSettings, showMyListings, showRefresh, showSort, showSearch);

        return new MainPageLayoutState(categories, history, sortType, null, buttonLayout);
    }

    //TODO: Methods to Get ButtonLayout alone

    private boolean isAllowedSearch(Player p, AllowedPlayerActions actions){
        if(actions.getSearch() == ActionState.TRUE) return true;
        if(actions.getSearch() == ActionState.FALSE) return false;
        return plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.search");
    }

    private boolean isAllowedSort(Player p, AllowedPlayerActions actions){
        if(actions.getSort() == ActionState.TRUE) return true;
        if(actions.getSort() == ActionState.FALSE) return false;
        return plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.sort");
    }

    private boolean isAllowedRefresh(Player p, AllowedPlayerActions actions){
        if(actions.getRefresh() == ActionState.TRUE) return true;
        if(actions.getRefresh() == ActionState.FALSE) return false;
        return plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.refresh");
    }

    private boolean isAllowedMyListings(Player p, AllowedPlayerActions actions){
        if(actions.getMyListings() == ActionState.TRUE) return true;
        if(actions.getMyListings() == ActionState.FALSE) return false;
        return plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.my_listings");
    }

    private boolean isAllowedSettings(Player p, AllowedPlayerActions actions){
        if(actions.getSettings() == ActionState.TRUE) return true;
        if(actions.getSettings() == ActionState.FALSE) return false;
        return plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.categories");
    }

    private MainPageLayoutState.ButtonLayout getCategoryLayout(Player p, PlayerPreferences preferences, AllowedPlayerActions allowedActions){
        if(allowedActions.getCategories()==ActionState.FALSE || (allowedActions.getCategories()==ActionState.UNDEFINED && !plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.categories"))){
            return  MainPageLayoutState.ButtonLayout.DISABLED;
        }
        return preferences.showCategories()? MainPageLayoutState.ButtonLayout.SIDEBAR : MainPageLayoutState.ButtonLayout.BUTTON;
    }

    private MainPageLayoutState.ButtonLayout getHistoryLayout(Player p, PlayerPreferences preferences, AllowedPlayerActions allowedActions){
        if(allowedActions.getHistory()==ActionState.FALSE || (allowedActions.getHistory()==ActionState.UNDEFINED && !plugin.getPermissionsProvider().hasPermission(p, "astraah.actions.history"))){
            return  MainPageLayoutState.ButtonLayout.DISABLED;
        }
        return preferences.showHistory()? MainPageLayoutState.ButtonLayout.SIDEBAR : MainPageLayoutState.ButtonLayout.BUTTON;
    }

}
