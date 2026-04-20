package dev.veyno.astraAH.data.dto;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Listing {

    private UUID listingId;
    private UUID sellerId;
    private ItemStack content;
    private long createdAt;
    private double price;
    private String currency;

}
