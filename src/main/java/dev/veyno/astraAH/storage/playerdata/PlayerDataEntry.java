package dev.veyno.astraAH.storage.playerdata;

import dev.veyno.astraAH.dto.TransactionHistory;
import dev.veyno.astraAH.dto.AllowedPlayerActions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;

public class PlayerDataEntry implements DataEntry {

    private UUID playerID;
    private TransactionHistory transactionHistory;
    private AllowedPlayerActions allowedActions;


    @Override
    public void write(DataOutputStream dos) {

    }

    @Override
    public void read(DataInputStream dis) {

    }


    public UUID getPlayerID() {
        return playerID;
    }
}
