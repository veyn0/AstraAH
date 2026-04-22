package dev.veyno.astraAH.app;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.ListingsService;
import dev.veyno.astraAH.data.dto.CachedListing;
import dev.veyno.astraAH.data.dto.Listing;

import java.util.List;
import java.util.UUID;

public class ListingController {

    private AstraAH plugin;
    private ListingsService listingsService;

    public ListingController(AstraAH plugin, ListingsService listingsService) {
        this.plugin = plugin;
        this.listingsService = listingsService;
    }

    public boolean purchaseListing(UUID listingId, UUID playerId){
        plugin.getLogger().info("Player " + playerId+ " tried to purchase listing " + listingId);
        // here removeIfpresent and transfering item to purchases.
        return false;
    }

    public boolean postListing(Listing l){
        listingsService.createListing(l);
        return true;
    }

    public boolean removeListingIfPresent(UUID listingId){
        return listingsService.removeListingIfPresent(listingId);
    }

    public List<CachedListing> getListings(){
        return listingsService.getCachedListings();
    }

    public CachedListing getListing(UUID listingId){
        return listingsService.getCachedListing(listingId);
    }

}
