package dev.veyno.astraAH.storage.history;

import dev.veyno.astraAH.entity.AHTransactionHistoryEntry;

import java.util.List;
import java.util.UUID;

public interface AHTransactionHistoryStorageProvider {

    void addEntry(AHTransactionHistoryEntry entry);

    List<AHTransactionHistoryEntry> getEntries(UUID playerId);

}
