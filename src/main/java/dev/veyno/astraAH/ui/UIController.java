package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UIController {

    private Map<UUID, UIState> playerUiStates = new HashMap<>();



    private final AstraAH plugin;

    public UIController(AstraAH plugin) {
        this.plugin = plugin;
    }


    public void onOpenAHUI(Player p){
        if(playerUiStates.get(p.getUniqueId())==null||!(playerUiStates.get(p.getUniqueId())==UIState.CLOSED));{
            plugin.getErrorHandler().onIllegalInventoryView(p);
            return;
        }


    }



    public void clearPlayerUIState(UUID pId){
        playerUiStates.put(pId, UIState.CLOSED);
    }
}
