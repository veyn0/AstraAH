package dev.veyno.astraAH.econ;

import dev.veyno.astraAH.AstraAH;

import java.util.UUID;

public interface EconomyConnector {

    public double getBalance(UUID playerId);

    public boolean withdraw(UUID playerId, double value);

    public boolean add(UUID playerId, double value);

}
