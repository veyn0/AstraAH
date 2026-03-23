package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ui.error.UIState;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UIController {

    private Map<UUID, UIState> playerUiStates = new HashMap<>();

    private final AstraAH plugin;

    private final Component AH_LISTINGS_TITLE;

    public UIController(AstraAH plugin) {
        this.plugin = plugin;
        FileConfiguration configuration = plugin.getConfig();
        this.AH_LISTINGS_TITLE = MiniMessage.miniMessage().deserialize(configuration.getString("messages.listings.title", "<red>Error 404. Content Not Found"));
    }

    public void onOpenAHUI(Player p){
        if(playerUiStates.get(p.getUniqueId())==null||!(playerUiStates.get(p.getUniqueId())==UIState.CLOSED));{
            plugin.getErrorHandler().onIllegalInventoryView(p);
            return;
        }





    }



    private void openMainPage(Player p){
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), AH_LISTINGS_TITLE, p);
    }




    public void clearPlayerUIState(UUID pId){
        playerUiStates.put(pId, UIState.CLOSED);
    }
}
