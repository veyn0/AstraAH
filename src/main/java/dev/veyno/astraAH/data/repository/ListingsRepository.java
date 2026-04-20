package dev.veyno.astraAH.data.repository;

import dev.veyno.astraAH.data.dto.Listing;

import java.util.List;
import java.util.UUID;

public interface ListingsRepository {

    void createListing(Listing listing);

    boolean removeIfPresent(UUID listingId);

    Listing getListing(UUID listingId);

    List<Listing> getActiveListings();

    List<Listing> getAllListings();

    List<Listing> getInactiveListings();

}
