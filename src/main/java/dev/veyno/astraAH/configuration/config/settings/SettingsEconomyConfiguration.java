package dev.veyno.astraAH.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;

public class SettingsEconomyConfiguration extends Configurable {

    private final double taxPercentage;
    private final String provider;

    public SettingsEconomyConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.taxPercentage = getDouble("tax_percentage", 10D);
        this.provider = getString("provider", "auto");
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public String getProvider() {
        return provider;
    }
}
