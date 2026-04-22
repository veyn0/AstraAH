package dev.veyno.astraAH.data;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.CachedListing;
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

    private Map<UUID, CachedListing> listingCache = new ConcurrentHashMap<>();

    private int cacheRefreshMillis;
    private int cacheTTLMillis;

    public ListingsService(AstraAH plugin, ListingsRepository listingsRepository, int cacheRefreshMillis, int cacheTTLMillis) {
        this.plugin = plugin;
        this.listingsRepository = listingsRepository;
        this.cacheRefreshMillis = cacheRefreshMillis;
        this.cacheTTLMillis = cacheTTLMillis;
        refreshCache();
        startRefreshSchedule();
    }

    private void refreshCache(){
        for(Listing l : listingsRepository.getAllListings()){
            renderToCache(l);
        }
        long cutoff = System.currentTimeMillis() - cacheTTLMillis;
        listingCache.entrySet().removeIf(e -> e.getValue().getLoadedAtMillis() < cutoff);
    }

    private void startRefreshSchedule(){
        Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task ->{
                    refreshCache();
                },
                1,
                cacheRefreshMillis,
                TimeUnit.MILLISECONDS
        );

    }

    public boolean removeListingIfPresent(UUID listingId){
        synchronized (IDLocks.getLock(listingId)) {
            if (listingsRepository.removeIfPresent(listingId)){
                listingCache.remove(listingId);
                return true;
            }
            else {
                return false;
            }
        }
    }

    public List<CachedListing> getCachedListings(){
        return listingCache.values().stream().toList();
    }

    public CachedListing getCachedListing(UUID listingId){
        return listingCache.get(listingId);
    }

    public Listing getListing(UUID listingId){
        synchronized (IDLocks.getLock(listingId)) {
            Listing result = listingsRepository.getListing(listingId);
            if(result==null) return null;
            renderToCache(result);
            return result;
        }
    }

    public void createListing(Listing listing){
        synchronized (IDLocks.getLock(listing.getListingId())) {
            listingsRepository.createListing(listing);
            renderToCache(listing);
        }
    }

    private void renderToCache(Listing l){
        CachedListing listing = new CachedListing(System.currentTimeMillis(), l);
        listingCache.put(l.getListingId(), listing);
    }

}

