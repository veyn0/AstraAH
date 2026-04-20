package dev.veyno.astraAH.data.dto;

public class CachedListing {
    private final long loadedAtMillis;
    private final Listing listing;

    public CachedListing(long loadedAt, Listing listing) {
        this.loadedAtMillis = loadedAt;
        this.listing = listing;
    }

    public long getLoadedAtMillis() {
        return loadedAtMillis;
    }

    public Listing getListing() {
        return listing;
    }
}
