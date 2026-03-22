package dev.veyno.astraAH.ah;

import org.bukkit.entity.Player;

import java.util.*;

public class AuctionHouse {

    private Map<UUID, Listing> listings = new HashMap<>();


    public List<Listing> getListings(){
        return null;
    }

    public List<Listing> getListings(UUID playerId){
        return null;
    }

    public Listing getListing(UUID listingId){
        return listings.get(listingId);
    }

    public boolean isListingAvailable(UUID listingId){
        return false;
    }

    public boolean onAttemptPurchase(Player buyer, UUID listingId){
        return false;
    }

    public boolean onAttemptRemove(UUID listingId){
        return false;
    }

}
