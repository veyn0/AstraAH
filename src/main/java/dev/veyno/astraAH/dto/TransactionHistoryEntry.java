package dev.veyno.astraAH.dto;

import dev.veyno.astraAH.storage.DataEntry;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class TransactionHistoryEntry implements DataEntry {

    private UUID entryId;
    private ItemStack content;
    private UUID playerId;
    private UUID sellerID;
    private double price;
    private Instant timeStamp;

    @Override
    public void write(DataOutputStream dos) throws IOException {

    }

    @Override
    public void read(DataInputStream dis) throws IOException {

    }
}
