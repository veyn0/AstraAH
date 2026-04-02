package dev.veyno.astraAH.storage.preferences.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.PlayerPreferences;
import dev.veyno.astraAH.entity.PlayerPreferencesCategoryEntry;
import dev.veyno.astraAH.storage.YamlStorage;
import dev.veyno.astraAH.storage.preferences.AHPlayerPreferencesStorageProvider;
import dev.veyno.astraAH.util.ItemStackUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FileAHPlayerPreferencesStorageProvider implements AHPlayerPreferencesStorageProvider {

    private final AstraAH plugin;
    private final YamlStorage storage;


    public FileAHPlayerPreferencesStorageProvider(AstraAH plugin) {
        this.plugin = plugin;
        this.storage = new YamlStorage(plugin, "preferences", false, 30);
    }

    @Override
    public PlayerPreferences getPreferences(UUID playerId) {
        synchronized (storage.getLock()) {
            return getPreferencesByPlayerId(playerId);
        }
    }

    @Override
    public void setPreferences(UUID playerId, PlayerPreferences preferences) {
        synchronized (storage.getLock()) {
            try {
                savePreferencesLocked(playerId, normalizePreferences(playerId, preferences));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save player preferences for " + playerId + ": " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        storage.saveFileAsync();
    }

    @Override
    public void addCategory(UUID playerId, PlayerPreferencesCategoryEntry categoryEntry) {
        if (categoryEntry == null) {
            return;
        }

        synchronized (storage.getLock()) {
            try {
                PlayerPreferences preferences = getPreferencesByPlayerId(playerId);
                if (preferences == null) {
                    preferences = new PlayerPreferences(playerId);
                }
                savePreferencesLocked(playerId, preferences.withAddedCategory(categoryEntry));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to add player preference category for " + playerId + ": " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        storage.saveFileAsync();
    }

    private PlayerPreferences getPreferencesByPlayerId(UUID playerId) {
        try {
            FileConfiguration config = storage.getFileConfiguration();
            String path = "preferences." + playerId;
            if (!config.contains(path + ".playerId")) {
                return null;
            }

            boolean showCategories = config.getBoolean(path + ".showCategories");
            boolean showHistory = config.getBoolean(path + ".showHistory");
            List<PlayerPreferencesCategoryEntry> categoryEntries = new ArrayList<>();
            ConfigurationSection categoriesSection = config.getConfigurationSection(path + ".categories");
            if (categoriesSection != null) {
                List<String> categoryKeys = new ArrayList<>(categoriesSection.getKeys(false));
                categoryKeys.sort(Comparator.comparingInt(Integer::parseInt));
                for (String categoryKey : categoryKeys) {
                    PlayerPreferencesCategoryEntry categoryEntry = getCategoryEntry(path + ".categories." + categoryKey);
                    if (categoryEntry != null) {
                        categoryEntries.add(categoryEntry);
                    }
                }
            }

            return new PlayerPreferences(playerId, categoryEntries, showCategories, showHistory);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load player preferences for " + playerId + ": " + e.getMessage());
            return null;
        }
    }

    private PlayerPreferencesCategoryEntry getCategoryEntry(String path) throws Exception {
        FileConfiguration config = storage.getFileConfiguration();
        String previewData = config.getString(path + ".preview");
        ItemStack preview = previewData == null ? null : ItemStackUtil.itemFromBase64(previewData);
        List<Material> filter = new ArrayList<>();
        for (String materialKey : config.getStringList(path + ".filter")) {
            Material material = Material.matchMaterial(materialKey);
            if (material != null) {
                filter.add(material);
            }
        }
        return new PlayerPreferencesCategoryEntry(preview, filter);
    }

    private void savePreferencesLocked(UUID playerId, PlayerPreferences preferences) throws Exception {
        FileConfiguration config = storage.getFileConfiguration();
        String path = "preferences." + playerId;
        config.set(path + ".playerId", playerId.toString());
        config.set(path + ".showCategories", preferences.showCategories());
        config.set(path + ".showHistory", preferences.showHistory());
        config.set(path + ".categories", null);

        int categoryIndex = 0;
        for (PlayerPreferencesCategoryEntry categoryEntry : preferences.categoryEntries()) {
            String categoryPath = path + ".categories." + categoryIndex;
            ItemStack preview = categoryEntry.preview();
            config.set(categoryPath + ".preview", preview == null ? null : ItemStackUtil.itemToBase64(preview));
            config.set(categoryPath + ".filter", categoryEntry.filter().stream().map(Material::name).toList());
            categoryIndex++;
        }
    }

    private PlayerPreferences normalizePreferences(UUID playerId, PlayerPreferences preferences) {
        if (preferences == null) {
            return new PlayerPreferences(playerId);
        }
        return new PlayerPreferences(
                playerId,
                preferences.categoryEntries(),
                preferences.showCategories(),
                preferences.showHistory()
        );
    }
}

