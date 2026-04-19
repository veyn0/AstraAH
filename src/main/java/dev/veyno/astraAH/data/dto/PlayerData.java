package dev.veyno.astraAH.data.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private UUID playerId;
    private Preferences preferences;
    private AllowedActions allowedActions;
    private List<Transaction> transactions;

    private long loadedAt;

    public static PlayerData configuredDefault(UUID playerId){
        PlayerData data = new PlayerData();
        data.setPlayerId(playerId);
        data.setPreferences(Preferences.configuredDefaults(playerId));
        data.setAllowedActions(AllowedActions.configuredDefaults(playerId));
        data.setTransactions(new ArrayList<>());
        return data;
    }


    public long getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(long loadedAt) {
        this.loadedAt = loadedAt;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public AllowedActions getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(AllowedActions allowedActions) {
        this.allowedActions = allowedActions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
