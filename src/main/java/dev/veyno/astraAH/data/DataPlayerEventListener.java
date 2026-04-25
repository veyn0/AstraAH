package dev.veyno.astraAH.data;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DataPlayerEventListener implements Listener {

    private final PlayerDataService playerDataService;

    public DataPlayerEventListener(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        playerDataService.onPlayerJoin(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        playerDataService.onPlayerQuit(e.getPlayer().getUniqueId());
    }

}
