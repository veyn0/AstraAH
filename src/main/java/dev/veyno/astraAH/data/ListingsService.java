package dev.veyno.astraAH.data;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.data.repository.ListingsRepository;
import dev.veyno.astraAH.util.IDLocks;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ListingsService {

    private AstraAH plugin;
    private ListingsRepository listingsRepository;

    private Map<UUID, Listing> listingCache = new ConcurrentHashMap<>();

    public ListingsService(AstraAH plugin, ListingsRepository listingsRepository) {
        this.plugin = plugin;
        this.listingsRepository = listingsRepository;
    }

    private void refreshCache(){
        Map<UUID, Listing> result = new ConcurrentHashMap<>();
        for(Listing l : listingsRepository.getAllListings()){
            result.put(l.getListingId(), l);
        }
        listingCache = result;
    }

    private void startRefreshSchedule(int seconds){
        Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task ->{
                    refreshCache();
                },
                1,
                seconds,
                TimeUnit.SECONDS
        );

    }

    public boolean removeListingBlocking(UUID listingId){
        synchronized (IDLocks.getLock(listingId)) {
            if (listingsRepository.removeIfPresent(listingId)){
                try {
                    listingCache.remove(listingId);
                }catch (Exception e){
                    plugin.getLogger().warning("Error while updating listings Cache");
                    e.printStackTrace();
                }
                return true;
            }
            else {
                return false;
            }
        }
    }

    public List<Listing> getCachedListings(){
        return listingCache.values().stream().toList();
    }

    public Listing getListingBypassCache(UUID listingId){
        synchronized (IDLocks.getLock(listingId)) {
            return listingsRepository.getListing(listingId);
        }
    }

    public void createListing(Listing listing){
        synchronized (IDLocks.getLock(listing.getListingId())) {
            listingsRepository.createListing(listing);
            listingCache.put(listing.getListingId(), listing);
        }
    }

}
