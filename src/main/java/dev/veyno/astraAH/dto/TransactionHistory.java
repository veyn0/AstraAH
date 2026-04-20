package dev.veyno.astraAH.dto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistory implements DataEntry {

    private List<TransactionHistoryEntry> transactionHistoryEntries = new ArrayList<>();

    @Override
    public void write(DataOutputStream dos) {

    }

    @Override
    public void read(DataInputStream dis) {

    }

}
