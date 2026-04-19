package dev.veyno.astraAH.entity;

import dev.veyno.astraAH.storage.DataEntry;
import dev.veyno.astraAH.util.ItemStackParser;
import dev.veyno.astraAH.util.ItemStackUtil;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
