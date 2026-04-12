package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ui.pages.MainPage;
import dev.veyno.astraAH.ui.pages.SettingsPage;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PageController {

    private final AstraAH plugin;

    private Map<UUID, MainPage> mainPages = new ConcurrentHashMap<>();

    private Map<UUID, SettingsPage> settingsPages = new ConcurrentHashMap<>();

    public PageController(AstraAH plugin) {
        this.plugin = plugin;
    }

    public void openMainPage(UUID playerId, boolean reload){
        MainPage page = getMainPage(playerId);
        if(reload) page.rebuild();
        page.open(null);
    }

    public void openSettingsPage(UUID playerId){
        SettingsPage page = getSettingsPage(playerId);
        page.rebuild();
        page.open(getMainPage(playerId));
    }

    public SettingsPage getSettingsPage(UUID playerId){
        if(!settingsPages.containsKey(playerId)) settingsPages.put(playerId, new SettingsPage(plugin, this, playerId) );
        return settingsPages.get(playerId);
    }

    public MainPage getMainPage(UUID playerId){
        if(!mainPages.containsKey(playerId)) mainPages.put(playerId, new MainPage(plugin, this, playerId, plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId))) );
        return mainPages.get(playerId);
    }




}
