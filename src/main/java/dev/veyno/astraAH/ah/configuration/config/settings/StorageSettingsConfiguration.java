package dev.veyno.astraAH.ah.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;

public class StorageSettingsConfiguration extends Configurable {

    private final String provider;
    private final String jdbc;

    public StorageSettingsConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.provider = getString("provider", "file");
        this.jdbc = getString("jdbc", "");
    }

    public String getProvider() {
        return provider;
    }

    public String getJdbc() {
        return jdbc;
    }
}
