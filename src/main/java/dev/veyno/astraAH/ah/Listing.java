package dev.veyno.astraAH.ah;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record Listing(
        UUID listingId,
        UUID playerId,
        double price,
        ItemStack content
) {
}
