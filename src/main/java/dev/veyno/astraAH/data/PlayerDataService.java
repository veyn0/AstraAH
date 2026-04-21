package dev.veyno.astraAH.data;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.data.repository.PlayerDataRepository;
import dev.veyno.astraAH.util.IDLocks;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerDataService {

    //TODO: add default values

    private PlayerDataRepository playerDataRepository;
    private AstraAH plugin;

    private static final int CACHE_LIFESPAN = 100_000;

    private Map<UUID, Long> playerQuitTimes = new ConcurrentHashMap<>();

    private Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    public PlayerDataService(PlayerDataRepository playerDataRepository, AstraAH plugin, int clearIntervalSeconds) {
        this.playerDataRepository = playerDataRepository;
        this.plugin = plugin;
        startClearCacheSchedule(clearIntervalSeconds);
    }

    public void onPlayerJoin(UUID playerId){
        synchronized (IDLocks.getLock(playerId)) {
            playerQuitTimes.remove(playerId);
            refreshPlayerData(playerId);
        }
    }

    public void onPlayerQuit(UUID playerId){
        synchronized (IDLocks.getLock(playerId)) {
        playerQuitTimes.put(playerId, System.currentTimeMillis());
        }
    }

    private void clearCache(){
        for(UUID id : new ArrayList<>(playerQuitTimes.keySet())){
            synchronized (IDLocks.getLock(id)) {
                if (playerQuitTimes.get(id) + CACHE_LIFESPAN < System.currentTimeMillis()) {
                    playerDataCache.remove(id);
                    playerQuitTimes.remove(id);
                }
            }
        }
    }

    private void startClearCacheSchedule(int intervalSeconds){
        Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task ->{
                    clearCache();
                },
                1,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }

    private void refreshPlayerData(UUID playerId){
        synchronized (IDLocks.getLock(playerId)) {
            PlayerData data = playerDataRepository.getPlayerData(playerId);
            if(data==null) data = PlayerData.configuredDefault(playerId);
            data.setLoadedAt(System.nanoTime());
            playerDataCache.put(playerId, data);
        }
    }

    public PlayerData getPlayerData(UUID playerId){
        synchronized (IDLocks.getLock(playerId)) {
            if (!playerDataCache.containsKey(playerId)) {
                plugin.getLogger().warning("PlayerData Cache not found as Expected for Player " + playerId.toString() + ". This might cause operations to slow down.");
                refreshPlayerData(playerId);
            }
            return playerDataCache.get(playerId);
        }
    }


    public void setPlayerData(PlayerData playerData, boolean force){
        synchronized (IDLocks.getLock(playerData.getPlayerId())) {
            PlayerData cached = playerDataCache.get(playerData.getPlayerId());
            if (cached != null && playerData.getLoadedAt() < cached.getLoadedAt()) {
                plugin.getLogger().severe("PlayerData Cache Invalid. Tried saving playerData but newer version is Present in cache");
                if(!force) return;
            }
            playerDataRepository.setPlayerData(playerData);
            playerDataCache.put(playerData.getPlayerId(), playerData);
        }
    }







}
