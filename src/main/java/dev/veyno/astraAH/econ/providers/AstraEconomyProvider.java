package dev.veyno.astraAH.econ.providers;

import dev.veyno.astraAH.econ.EconomyProvider;

import java.util.UUID;

public class AstraEconomyProvider implements EconomyProvider {

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
