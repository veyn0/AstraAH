package dev.veyno.astraAH.econ.provider;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.econ.EconomyProvider;

import java.util.UUID;

public class FileEconomyProvider implements EconomyProvider {

    private final AstraAH plugin;

    public FileEconomyProvider(AstraAH plugin) {
        this.plugin = plugin;
    }

    @Override
    public double getBalance(UUID playerId) {
        return 0;
    }

    @Override
    public boolean withdraw(UUID playerId, double value) {
        return false;
    }

    @Override
    public boolean add(UUID playerId, double value) {
        return false;
    }
}
