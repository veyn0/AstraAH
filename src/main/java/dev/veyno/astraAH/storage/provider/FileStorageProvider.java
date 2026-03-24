package dev.veyno.astraAH.storage.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.Listing;
import dev.veyno.astraAH.storage.StorageProvider;

import java.util.List;
import java.util.UUID;

public class FileStorageProvider implements StorageProvider {

    private final AstraAH plugin;


    public FileStorageProvider(AstraAH plugin) {
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
