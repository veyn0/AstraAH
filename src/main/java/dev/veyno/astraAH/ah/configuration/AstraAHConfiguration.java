package dev.veyno.astraAH.ah.configuration;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.ConfiguredCategories;
import dev.veyno.astraAH.ah.configuration.config.ConfiguredEconomy;
import dev.veyno.astraAH.ah.configuration.config.ConfiguredGuis;
import dev.veyno.astraAH.ah.configuration.config.ConfiguredSettings;

public class AstraAHConfiguration {

    private final ConfiguredGuis configuredGuis;
    private final ConfiguredCategories configuredCategories;
    private final ConfiguredSettings configuredSettings;
    private final ConfiguredEconomy configuredEconomy;

    public AstraAHConfiguration(AstraAH plugin) {
        this.configuredGuis = new ConfiguredGuis(plugin, "guis");
        this.configuredCategories = new ConfiguredCategories(plugin, "categories");
        this.configuredSettings = new ConfiguredSettings(plugin, "settings");
        this.configuredEconomy = new ConfiguredEconomy(plugin, "economy");
    }

    public ConfiguredGuis getConfiguredGuis() {
        return configuredGuis;
    }

    public ConfiguredCategories getConfiguredCategories() {
        return configuredCategories;
    }

    public ConfiguredSettings getConfiguredSettings() {
        return configuredSettings;
    }

    public ConfiguredEconomy getConfiguredEconomy() {
        return configuredEconomy;
    }
}
