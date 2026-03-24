package dev.veyno.astraAH.storage.provider;

import dev.veyno.astraAH.ah.Listing;
import dev.veyno.astraAH.storage.StorageProvider;

import java.util.List;
import java.util.UUID;

public class JDBCDatabaseStorageProvider implements StorageProvider {

    @Override
    public void saveListing(Listing listing) {

    }

    @Override
    public Listing getListing(UUID listingId) {
        return null;
    }

    @Override
    public List<Listing> getListings() {
        return List.of();
    }

    @Override
    public void removeListing(UUID listingId) {

    }
}
