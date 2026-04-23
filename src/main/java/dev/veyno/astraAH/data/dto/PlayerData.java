package dev.veyno.astraAH.data.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private final UUID playerId;
    private final Preferences preferences;
    private final AllowedActions allowedActions;
    private final List<Transaction> transactions;

    // only relevant for cache
    private long loadedAt;

    public PlayerData(UUID playerId,
                      Preferences preferences,
                      AllowedActions allowedActions,
                      List<Transaction> transactions,
                      long loadedAt) {
        this.playerId = playerId;
        this.preferences = preferences;
        this.allowedActions = allowedActions;
        this.transactions = transactions == null ? List.of() : List.copyOf(transactions);
        this.loadedAt = loadedAt;
    }

    public static PlayerData configuredDefault(UUID playerId) {
        return new PlayerData(
                playerId,
                Preferences.configuredDefaults(playerId),
                AllowedActions.configuredDefaults(playerId),
                new ArrayList<>(),
                0L
        );
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public AllowedActions getAllowedActions() {
        return allowedActions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public long getLoadedAt() {
        return loadedAt;
    }

    public PlayerData withPlayerId(UUID playerId) {
        return new PlayerData(playerId, preferences, allowedActions, transactions, loadedAt);
    }

    public PlayerData withPreferences(Preferences preferences) {
        return new PlayerData(playerId, preferences, allowedActions, transactions, loadedAt);
    }

    public PlayerData withAllowedActions(AllowedActions allowedActions) {
        return new PlayerData(playerId, preferences, allowedActions, transactions, loadedAt);
    }

    public PlayerData withTransactions(List<Transaction> transactions) {
        return new PlayerData(playerId, preferences, allowedActions, transactions, loadedAt);
    }

    public PlayerData withAddedTransaction(Transaction transaction) {
        List<Transaction> updated = new ArrayList<>(transactions);
        updated.add(transaction);
        return withTransactions(updated);
    }

    public PlayerData withLoadedAt(long loadedAt) {
        return new PlayerData(playerId, preferences, allowedActions, transactions, loadedAt);
    }

    public void setLoadedAt(long loadedAt) {
        this.loadedAt = loadedAt;
    }
}