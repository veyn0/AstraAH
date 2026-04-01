package dev.veyno.astraAH.ah.configuration.config;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import dev.veyno.astraAH.ah.configuration.config.settings.GuiSettingsConfiguration;
import dev.veyno.astraAH.ah.configuration.config.settings.SettingsEconomyConfiguration;
import dev.veyno.astraAH.ah.configuration.config.settings.StorageSettingsConfiguration;

public class ConfiguredSettings extends Configurable {

    private final GuiSettingsConfiguration guiSettingsConfiguration;
    private final SettingsEconomyConfiguration economySettingsConfiguration;
    private final StorageSettingsConfiguration storageSettingsConfiguration;
    private final String language;
    private final boolean categoriesEnabled;

    public ConfiguredSettings(AstraAH plugin, String path) {
        super(path, plugin);

        this.guiSettingsConfiguration = new GuiSettingsConfiguration(getPlugin(), resolvePath("gui"));
        this.economySettingsConfiguration = new SettingsEconomyConfiguration(getPlugin(), resolvePath("economy"));
        this.storageSettingsConfiguration = new StorageSettingsConfiguration(getPlugin(), resolvePath("storage"));
        this.language = getString("language", "en_us");
        this.categoriesEnabled = getBoolean("categories", true);
    }

    public GuiSettingsConfiguration getGuiSettingsConfiguration() {
        return guiSettingsConfiguration;
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
