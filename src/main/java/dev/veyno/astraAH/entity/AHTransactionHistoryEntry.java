package dev.veyno.astraAH.entity;

import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.UUID;


@Deprecated()
public record AHTransactionHistoryEntry(
        UUID entryId,
        ItemStack content,
        UUID playerId,
        UUID sellerID,
        double price,
        Instant timeStamp
) {}
