package dev.veyno.astraAH.configuration.config;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import dev.veyno.astraAH.configuration.config.settings.SettingsActionsConfiguration;
import dev.veyno.astraAH.configuration.config.settings.SettingsDefaultsConfiguration;
import dev.veyno.astraAH.configuration.config.settings.SettingsEconomyConfiguration;
import dev.veyno.astraAH.configuration.config.settings.StorageSettingsConfiguration;

public class ConfiguredSettings extends Configurable {

    private final SettingsActionsConfiguration actionsConfiguration;
    private final SettingsDefaultsConfiguration defaultsConfiguration;
    private final SettingsEconomyConfiguration economySettingsConfiguration;
    private final StorageSettingsConfiguration storageSettingsConfiguration;
    private final String language;
    private final boolean categoriesEnabled;

    public ConfiguredSettings(AstraAH plugin, String path) {
        super(path, plugin);

        this.actionsConfiguration = new SettingsActionsConfiguration(getPlugin(), resolvePath("actions"));
        this.defaultsConfiguration = new SettingsDefaultsConfiguration(getPlugin(), resolvePath("defaults"));
        this.economySettingsConfiguration = new SettingsEconomyConfiguration(getPlugin(), resolvePath("economy"));
        this.storageSettingsConfiguration = new StorageSettingsConfiguration(getPlugin(), resolvePath("storage"));
        this.language = getString("language", "en_us");
        this.categoriesEnabled = getBoolean("categories", true);
    }

    public SettingsActionsConfiguration getActionsConfiguration() {
        return actionsConfiguration;
    }

    public SettingsDefaultsConfiguration getDefaultsConfiguration() {
        return defaultsConfiguration;
    }

    public SettingsEconomyConfiguration getEconomySettingsConfiguration() {
        return economySettingsConfiguration;
    }

    public StorageSettingsConfiguration getStorageSettingsConfiguration() {
        return storageSettingsConfiguration;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isCategoriesEnabled() {
        return categoriesEnabled;
    }
}
