package dev.veyno.astraAH.ah.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;

public class GuiSettingsConfiguration extends Configurable {

    private final DefaultLayoutConfiguration defaultLayoutConfiguration;

    public GuiSettingsConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.defaultLayoutConfiguration = new DefaultLayoutConfiguration(getPlugin(), resolvePath("default_layout"));
    }

    public DefaultLayoutConfiguration getDefaultLayoutConfiguration() {
        return defaultLayoutConfiguration;
    }
}
