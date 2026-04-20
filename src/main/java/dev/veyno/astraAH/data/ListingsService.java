package dev.veyno.astraAH.data;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.data.repository.ListingsRepository;
import org.bukkit.Bukkit;

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

    }



}
