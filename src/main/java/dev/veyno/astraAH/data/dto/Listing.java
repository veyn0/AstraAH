package dev.veyno.astraAH.data.dto;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Listing {

    private final UUID listingId;
    private final UUID sellerId;
    private final ItemStack content;
    private final long createdAt;
    private final double price;
    private final String currency;
    private int status;

    public Listing(UUID listingId, UUID sellerId, ItemStack content, long createdAt, double price, String currency, int status) {
        this.listingId = listingId;
        this.sellerId = sellerId;
        this.content = content;
        this.createdAt = createdAt;
        this.price = price;
        this.currency = currency;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public UUID getListingId() {
        return listingId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public ItemStack getContent() {
        return content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }
}
