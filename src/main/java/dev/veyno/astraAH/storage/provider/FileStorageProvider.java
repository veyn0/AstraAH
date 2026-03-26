package dev.veyno.astraAH.storage.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.Listing;
import dev.veyno.astraAH.storage.StorageProvider;
import dev.veyno.astraAH.util.YamlStorage;

import java.util.List;
import java.util.UUID;

public class FileStorageProvider implements StorageProvider {

    private final AstraAH plugin;

    private final YamlStorage storage;

    public FileStorageProvider(AstraAH plugin) {
        this.plugin = plugin;
        this.storage = new YamlStorage(plugin, "data.yml", true, 30);
    }

    @Override
    public void saveListing(Listing listing){

    }

    @Override
    public Listing getListing(UUID listingId) {
        return null;
    }

    @Override
    public List<Listing> getListings(){
        return null;
    }

    @Override
    public void removeListing(UUID listingId){

    }

}
