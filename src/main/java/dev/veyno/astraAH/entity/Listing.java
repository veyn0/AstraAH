package dev.veyno.astraAH.entity;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Deprecated(forRemoval = true)
public record Listing(
        UUID listingId,
        UUID playerId,
        double price,
        ItemStack content
) {
}
