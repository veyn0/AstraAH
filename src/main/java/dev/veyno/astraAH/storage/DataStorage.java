package dev.veyno.astraAH.storage;

import dev.veyno.astraAH.storage.playerdata.PlayerDataEntry;
import dev.veyno.astraAH.util.IDLocks;

import java.nio.file.Path;
import java.util.UUID;

public class DataStorage {


    private final int version = 0;

    private Path dataPath;

    public DataStorage(Path dataPath){
        this.dataPath = dataPath;
    }

    public PlayerDataEntry getPlayerData(UUID playerId){
        synchronized (IDLocks.getLock(playerId)){

        }
    }

    public void setPlayerData(PlayerDataEntry playerData){
        synchronized (IDLocks.getLock(playerData.getPlayerID())){

        }
    }



}
