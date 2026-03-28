package dev.veyno.astraAH.entity;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record Listing(
        UUID listingId,
        UUID playerId,
        double price,
        ItemStack content
) {
}
