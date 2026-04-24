package dev.veyno.astraAH.configuration.config;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;

public class ConfiguredEconomy extends Configurable {

    private final double defaultBalance;

    public ConfiguredEconomy(AstraAH plugin, String path) {
        super(path, plugin);

        this.defaultBalance = getDouble("default_balance", 1000D);
    }

    public double getDefaultBalance() {
        return defaultBalance;
    }
}
