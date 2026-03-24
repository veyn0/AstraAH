package dev.veyno.astraAH.storage;

import dev.veyno.astraAH.ah.Listing;

import java.util.List;
import java.util.UUID;

public interface StorageProvider {
    void saveListing(Listing listing);
    Listing getListing(UUID listingId);
    List<Listing> getListings();
    void removeListing(UUID listingId);
}
