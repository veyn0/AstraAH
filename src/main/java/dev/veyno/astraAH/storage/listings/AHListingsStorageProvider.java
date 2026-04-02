package dev.veyno.astraAH.storage.listings;

import dev.veyno.astraAH.entity.Listing;

import java.util.List;
import java.util.UUID;

public interface AHListingsStorageProvider {
    void saveListing(Listing listing) throws Exception;
    Listing getListing(UUID listingId);
    List<Listing> getListings();
    void removeListing(UUID listingId);
}
