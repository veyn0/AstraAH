package dev.veyno.astraAH.data.repository;

import dev.veyno.astraAH.data.dto.PlayerData;

import java.util.List;
import java.util.UUID;

public interface PlayerDataRepository {

    PlayerData getPlayerData(UUID playerId);

    List<PlayerData> getPlayerData();

    List<PlayerData> getPlayerData(List<UUID> playerIds);

    List<PlayerData> getPlayerData(UUID... playerIds);

    void setPlayerData(PlayerData playerData);

    void setPlayerData(List<PlayerData> playerData);

    void setPlayerData(PlayerData... playerData);
}
