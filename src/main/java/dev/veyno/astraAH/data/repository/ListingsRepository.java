package dev.veyno.astraAH.data.repository;

import java.util.UUID;

public interface ListingsRepository {

    boolean removeIfPresent(UUID listingId);



}
