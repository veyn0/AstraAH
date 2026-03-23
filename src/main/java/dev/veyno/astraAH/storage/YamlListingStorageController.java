package dev.veyno.astraAH.storage;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.Listing;

import java.util.List;
import java.util.UUID;

public class YamlListingStorageController implements ListingStorage{

    private final AstraAH plugin;


    public YamlListingStorageController(AstraAH plugin) {
        this.plugin = plugin;
    }

    public void saveListing(Listing listing){

    }

    @Override
    public Listing getListing(UUID listingId) {
        return null;
    }

    public List<Listing> getListings(){
        return null;
    }

    public void removeListing(UUID listingId){

    }

}
