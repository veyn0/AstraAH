package dev.veyno.astraAH.storage.history.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.AHTransactionHistoryEntry;
import dev.veyno.astraAH.data.YamlStorage;
import dev.veyno.astraAH.storage.history.AHTransactionHistoryStorageProvider;
import dev.veyno.astraAH.data.serialization.ItemStackBase64Serializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FileAHTransactionHistoryStorageProvider implements AHTransactionHistoryStorageProvider {

    private final AstraAH plugin;
    private final YamlStorage storage;

    public FileAHTransactionHistoryStorageProvider(AstraAH plugin) {
        this.plugin = plugin;
        this.storage = new YamlStorage(plugin, "transactions", false, 30);
    }

    @Override
    public void addEntry(AHTransactionHistoryEntry entry) {
        if (entry == null) {
            return;
        }

        synchronized (storage.getLock()) {
            try {
                FileConfiguration config = storage.getFileConfiguration();
                String path = "transactions." + entry.playerId() + "." + entry.entryId();
                config.set(path + ".entryId", entry.entryId().toString());
                config.set(path + ".playerId", entry.playerId().toString());
                config.set(path + ".sellerId", entry.sellerID().toString());
                config.set(path + ".price", entry.price());
                config.set(path + ".timestamp", entry.timeStamp().toEpochMilli());
                config.set(path + ".content", ItemStackBase64Serializer.itemToBase64(entry.content()));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save transaction history entry " + entry.entryId() + ": " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        storage.saveFileAsync();
    }

    @Override
    public List<AHTransactionHistoryEntry> getEntries(UUID playerId) {
        synchronized (storage.getLock()) {
            FileConfiguration config = storage.getFileConfiguration();
            ConfigurationSection section = config.getConfigurationSection("transactions." + playerId);
            if (section == null) {
                return List.of();
            }

            List<AHTransactionHistoryEntry> result = new ArrayList<>();
            for (String entryKey : section.getKeys(false)) {
                AHTransactionHistoryEntry entry = getEntry(playerId, entryKey);
                if (entry != null) {
                    result.add(entry);
                }
            }
            result.sort(Comparator.comparing(AHTransactionHistoryEntry::timeStamp).reversed());
            return List.copyOf(result);
        }
    }

    private AHTransactionHistoryEntry getEntry(UUID playerId, String entryKey) {
        try {
            FileConfiguration config = storage.getFileConfiguration();
            String path = "transactions." + playerId + "." + entryKey;
            if (!config.contains(path + ".entryId")) {
                return null;
            }

            UUID entryId = UUID.fromString(config.getString(path + ".entryId"));
            UUID storedPlayerId = UUID.fromString(config.getString(path + ".playerId"));
            UUID sellerId = UUID.fromString(config.getString(path + ".sellerId"));
            double price = config.getDouble(path + ".price");
            long timestamp = config.getLong(path + ".timestamp");
            ItemStack content = ItemStackBase64Serializer.itemFromBase64(config.getString(path + ".content"));

            return new AHTransactionHistoryEntry(
                    entryId,
                    content,
                    storedPlayerId,
                    sellerId,
                    price,
                    Instant.ofEpochMilli(timestamp)
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load transaction history entry " + entryKey + " for " + playerId + ": " + e.getMessage());
            return null;
        }
    }
}
