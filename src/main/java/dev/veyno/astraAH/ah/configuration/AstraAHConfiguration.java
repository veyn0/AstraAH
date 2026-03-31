package dev.veyno.astraAH.ah.configuration;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.ConfiguredGuis;

public class AstraAHConfiguration {

    private final AstraAH plugin;

    private ConfiguredGuis configuredGuis;

    public AstraAHConfiguration(AstraAH plugin) {
        this.plugin = plugin;

        this.configuredGuis = new ConfiguredGuis(plugin, "guis");

    }

    public ConfiguredGuis getConfiguredGuis() {
        return configuredGuis;
    }
}
