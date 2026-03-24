package dev.veyno.astraAH.econ;

import java.util.UUID;

public interface EconomyProvider {

    public double getBalance(UUID playerId);

    public boolean withdraw(UUID playerId, double value);

    public boolean add(UUID playerId, double value);

}
