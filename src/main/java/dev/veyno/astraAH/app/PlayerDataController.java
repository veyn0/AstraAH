package dev.veyno.astraAH.app;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.PlayerDataService;
import dev.veyno.astraAH.data.dto.PlayerData;

import java.util.UUID;

public class PlayerDataController {

    private AstraAH plugin;
    private PlayerDataService playerDataService;
    private PlayerDataManager playerDataManager;

    public PlayerDataController(AstraAH plugin, PlayerDataService playerDataService) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
        this.playerDataManager = new PlayerDataManager(plugin, playerDataService);
    }

    public PlayerData getPlayerData(UUID playerId){
        return playerDataService.getPlayerData(playerId);
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
