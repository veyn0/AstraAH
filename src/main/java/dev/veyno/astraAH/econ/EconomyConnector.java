package dev.veyno.astraAH.econ;

import dev.veyno.astraAH.AstraAH;

import java.util.UUID;

public class EconomyConnector {

    private final AstraAH plugin;

    public EconomyConnector(AstraAH plugin) {
        this.plugin = plugin;
    }

    public double getBalance(UUID playerId){
        return 0;
    }

    public boolean withdraw(UUID playerId, double value){
        return false;
    }

    public boolean add(UUID playerId, double value){
        return false;
    }

}
