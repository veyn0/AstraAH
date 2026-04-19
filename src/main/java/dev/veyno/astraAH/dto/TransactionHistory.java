package dev.veyno.astraAH.dto;

import dev.veyno.astraAH.storage.DataEntry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionHistory implements DataEntry {

    private List<TransactionHistoryEntry> transactionHistoryEntries = new ArrayList<>();

    @Override
    public void write(DataOutputStream dos) {

    }

    @Override
    public void read(DataInputStream dis) {

    }

}
