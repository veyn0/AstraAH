package dev.veyno.astraAH.econ;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultEconomyConnector implements EconomyConnector {

    private final Economy economy;

    public VaultEconomyConnector(Economy economy) {
        this.economy = economy;
    }

    public static EconomyConnector createOrNull() {
        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        return rsp != null ? new VaultEconomyConnector(rsp.getProvider()) : null;
    }

    private OfflinePlayer getPlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Override
    public double getBalance(UUID playerId) {
        return economy.getBalance(getPlayer(playerId));
    }

    @Override
    public boolean withdraw(UUID playerId, double value) {
        OfflinePlayer player = getPlayer(playerId);

        if (!economy.has(player, value)) {
            return false;
        }

        return economy.withdrawPlayer(player, value).transactionSuccess();
    }

    @Override
    public boolean add(UUID playerId, double value) {
        OfflinePlayer player = getPlayer(playerId);
        return economy.depositPlayer(player, value).transactionSuccess();
    }
}