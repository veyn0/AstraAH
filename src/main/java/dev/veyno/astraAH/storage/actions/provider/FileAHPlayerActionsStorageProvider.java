package dev.veyno.astraAH.storage.actions.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.page.ActionState;
import dev.veyno.astraAH.entity.page.AllowedPlayerActions;
import dev.veyno.astraAH.entity.page.PreferencesPlayerActions;
import dev.veyno.astraAH.storage.YamlStorage;
import dev.veyno.astraAH.storage.actions.AHPlayerActionsStorageProvider;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class FileAHPlayerActionsStorageProvider implements AHPlayerActionsStorageProvider {

    private final AstraAH plugin;
    private final YamlStorage storage;

    public FileAHPlayerActionsStorageProvider(AstraAH plugin) {
        this.plugin = plugin;
        this.storage = new YamlStorage(plugin, "actions", false, 30);
    }

    @Override
    public AllowedPlayerActions getAllowedActions(UUID playerId) {
        synchronized (storage.getLock()) {
            return getAllowedActionsByPlayerId(playerId);
        }
    }

    @Override
    public void saveAllowedActions(AllowedPlayerActions actions) {
        if (actions == null || actions.getPlayerId() == null) {
            return;
        }

        synchronized (storage.getLock()) {
            try {
                saveAllowedActionsLocked(normalizeAllowedActions(actions));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save allowed player actions for " + actions.getPlayerId() + ": " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        storage.saveFileAsync();
    }

    private AllowedPlayerActions getAllowedActionsByPlayerId(UUID playerId) {
        try {
            FileConfiguration config = storage.getFileConfiguration();
            String path = "actions." + playerId;
            if (!config.contains(path + ".playerId")) {
                return null;
            }

            UUID storedPlayerId = UUID.fromString(config.getString(path + ".playerId"));
            PreferencesPlayerActions preferencesPlayerActions = new PreferencesPlayerActions(
                    getActionState(config.getString(path + ".preferencesPlayerActions.showAdvancedCategories")),
                    getActionState(config.getString(path + ".preferencesPlayerActions.showAdvancedHistory")),
                    getActionState(config.getString(path + ".preferencesPlayerActions.reloadOnOpen")),
                    getActionState(config.getString(path + ".preferencesPlayerActions.defaultFilter")),
                    getActionState(config.getString(path + ".preferencesPlayerActions.defaultSort"))
            );

            return new AllowedPlayerActions(
                    storedPlayerId,
                    preferencesPlayerActions,
                    getActionState(config.getString(path + ".categories")),
                    getActionState(config.getString(path + ".settings")),
                    getActionState(config.getString(path + ".myListings")),
                    getActionState(config.getString(path + ".refresh")),
                    getActionState(config.getString(path + ".sort")),
                    getActionState(config.getString(path + ".search")),
                    getActionState(config.getString(path + ".history"))
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load allowed player actions for " + playerId + ": " + e.getMessage());
            return null;
        }
    }

    private void saveAllowedActionsLocked(AllowedPlayerActions actions) {
        FileConfiguration config = storage.getFileConfiguration();
        String path = "actions." + actions.getPlayerId();
        config.set(path + ".playerId", actions.getPlayerId().toString());
        config.set(path + ".preferencesPlayerActions.showAdvancedCategories", actions.getPreferencesPlayerActions().getShowAdvancedCategories().name());
        config.set(path + ".preferencesPlayerActions.showAdvancedHistory", actions.getPreferencesPlayerActions().getShowAdvancedHistory().name());
        config.set(path + ".preferencesPlayerActions.reloadOnOpen", actions.getPreferencesPlayerActions().getReloadOnOpen().name());
        config.set(path + ".preferencesPlayerActions.defaultFilter", actions.getPreferencesPlayerActions().getDefaultFilter().name());
        config.set(path + ".preferencesPlayerActions.defaultSort", actions.getPreferencesPlayerActions().getDefaultSort().name());
        config.set(path + ".categories", actions.getCategories().name());
        config.set(path + ".settings", actions.getSettings().name());
        config.set(path + ".myListings", actions.getMyListings().name());
        config.set(path + ".refresh", actions.getRefresh().name());
        config.set(path + ".sort", actions.getSort().name());
        config.set(path + ".search", actions.getSearch().name());
        config.set(path + ".history", actions.getHistory().name());
    }

    private AllowedPlayerActions normalizeAllowedActions(AllowedPlayerActions actions) {
        return new AllowedPlayerActions(
                actions.getPlayerId(),
                normalizePreferencesActions(actions.getPreferencesPlayerActions()),
                actions.getCategories(),
                actions.getSettings(),
                actions.getMyListings(),
                actions.getRefresh(),
                actions.getSort(),
                actions.getSearch(),
                actions.getHistory()
        );
    }

    private PreferencesPlayerActions normalizePreferencesActions(PreferencesPlayerActions preferencesActions) {
        if (preferencesActions == null) {
            return new PreferencesPlayerActions();
        }

        return new PreferencesPlayerActions(
                preferencesActions.getShowAdvancedCategories(),
                preferencesActions.getShowAdvancedHistory(),
                preferencesActions.getReloadOnOpen(),
                preferencesActions.getDefaultFilter(),
                preferencesActions.getDefaultSort()
        );
    }

    private ActionState getActionState(String serializedValue) {
        if (serializedValue == null) {
            return ActionState.UNDEFINED;
        }

        try {
            return ActionState.valueOf(serializedValue);
        } catch (IllegalArgumentException e) {
            return ActionState.UNDEFINED;
        }
    }
}
