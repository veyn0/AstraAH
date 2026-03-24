package dev.veyno.astraAH.ah;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.storage.ListingStorage;
import dev.veyno.astraAH.util.IDLocks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AuctionHouse {

    private final AstraAH plugin;
    private final ListingStorage storage;

    private Map<UUID, Listing> listings = new ConcurrentHashMap<>();

    public AuctionHouse(AstraAH plugin, ListingStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        startCacheRefreshSchedule();
    }

    public List<Listing> getListings(){
        return listings.values().stream().toList();
    }

    public List<Listing> getListings(UUID playerId){
        return null;
    }

    public Listing getListing(UUID listingId){
        return listings.get(listingId);
    }

    public boolean isListingAvailable(UUID listingId){
        return false;
    }

    public boolean onAttemptPurchase(Player buyer, UUID listingId){
        return false;
    }

    public boolean onAttemptRemove(UUID listingId){
        return false;
    }

    public void onListingUpdate(UUID listingId){
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

    private Listing refreshListing(UUID listingId){
        synchronized (IDLocks.getLock(listingId)) {
            Listing result = storage.getListing(listingId);
            listings.put(listingId, result);
            return result;
        }
    }



}
