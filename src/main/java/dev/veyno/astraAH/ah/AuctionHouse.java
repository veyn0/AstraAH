package dev.veyno.astraAH.ah;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.econ.EconomyProvider;
import dev.veyno.astraAH.storage.StorageProvider;
import dev.veyno.astraAH.util.IDLocks;
import dev.veyno.astraAH.util.PurchaseResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AuctionHouse {

    private final AstraAH plugin;
    private final StorageProvider storage;
    private final EconomyProvider economy;

    private Map<UUID, Listing> listings = new ConcurrentHashMap<>();

    public AuctionHouse(AstraAH plugin, StorageProvider storage, EconomyProvider economy) {
        this.plugin = plugin;
        this.storage = storage;
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

    public boolean isListingAvailableBlocking(UUID listingId){
        synchronized (IDLocks.getLock(listingId)){
            return storage.getListing(listingId) != null;
        }
    }

    public PurchaseResult onAttemptPurchase(Player buyer, UUID listingId){
        Listing listing = onListingUpdateBlocking(listingId);
        synchronized (IDLocks.getLock(listingId)){
            if(storage.getListing(listingId)==null) return PurchaseResult.UNAVAILABLE;
            if(!economy.withdraw(buyer.getUniqueId(), listing.price())) return PurchaseResult.MISSING_FUNDS;
            if(!economy.add(listing.playerId(), listing.price())) return PurchaseResult.ERROR;



            return PurchaseResult.SUCCESS;
        }
    }

    public boolean onAttemptRemove(UUID listingId){
        synchronized (IDLocks.getLock(listingId)){
            storage.removeListing(listingId);
            removeListingFromCache(listingId);
        }
        return false;
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

    private void startCacheRefreshSchedule(){
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            refreshCache();
        }, 1, 120, TimeUnit.SECONDS);
    }

    private synchronized void refreshCache(){
        try {
            List<Listing> listingsRaw = storage.getListings();
            Map<UUID, Listing> result = new ConcurrentHashMap<>();
            for(Listing l : listingsRaw){
                result.put(l.listingId(), l);
            }
            listings = result;
        } catch (Exception e){
            plugin.getLogger().severe("Failed to refresh AuctionHouse Listings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeListingFromCache(UUID listingId) {
        listings.remove(listingId);
    }



}
