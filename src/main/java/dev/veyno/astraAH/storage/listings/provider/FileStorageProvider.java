package dev.veyno.astraAH.storage.listings.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.storage.listings.StorageProvider;
import dev.veyno.astraAH.util.ItemStackUtil;
import dev.veyno.astraAH.util.YamlStorage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileStorageProvider implements StorageProvider {

    private final AstraAH plugin;

    private final YamlStorage storage;

    public FileStorageProvider(AstraAH plugin) {
        this.plugin = plugin;
        this.storage = new YamlStorage(plugin, "data", true, 30);
    }

    @Override
    public void saveListing(Listing listing) throws Exception {
        synchronized (storage.getLock()) {
            FileConfiguration config = storage.getFileConfiguration();
            String path = "listings."+listing.listingId().toString();
            config.set(path+".listingId", listing.listingId().toString());
            config.set(path+".price", listing.price());
            config.set(path+".playerId", listing.playerId().toString());
            config.set(path+".content", ItemStackUtil.itemToBase64(listing.content()));
        }
    }

    @Override
    public Listing getListing(UUID listingId) {
        synchronized (storage.getLock()) {
            return getListingById(listingId);
        }
    }

    @Override
    public List<Listing> getListings(){
        synchronized (storage.getLock()) {
            FileConfiguration config = storage.getFileConfiguration();
            List<Listing> result = new ArrayList<>();
            for(String key : config.getConfigurationSection("listings").getKeys(false)){
            result.add(getListingById(UUID.fromString(key)));
            }
            return result.isEmpty() ? null : result;
        }
    }

    @Override
    public void removeListing(UUID listingId){
        synchronized (storage.getLock()){
            FileConfiguration config = storage.getFileConfiguration();
            config.set("listings."+listingId.toString(), null);
        }
        storage.saveFileAsync();
    }

    private Listing getListingById(UUID listingId){
        try {
            String path = "listings."+listingId.toString();
            FileConfiguration config = storage.getFileConfiguration();
            double price = config.getDouble(path+".price");
            UUID playerId = UUID.fromString(config.getString(path+".playerId"));
            ItemStack content = ItemStackUtil.itemFromBase64(config.getString(path+".content"));
            return new Listing(listingId, playerId, price, content);
        }
        catch (Exception e){
            return null;
        }
    }
}
