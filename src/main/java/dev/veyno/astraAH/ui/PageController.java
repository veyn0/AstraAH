package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.page.mainpage.MainPageLayoutState;
import dev.veyno.astraAH.ui.pages.MainPage;
import dev.veyno.astraAH.ui.pages.SettingsPage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PageController {

    private final AstraAH plugin;

    private MainPage mainPage;
    private SettingsPage settingsPage;
    private Map<UUID, MainPageLayoutState> playerPageLayoutStates = new ConcurrentHashMap<>();

    public PageController(AstraAH plugin) {
        this.plugin = plugin;
        this.mainPage = new MainPage(plugin, this);
        this.settingsPage = new SettingsPage(plugin, this);
    }

    public void openMainPage(Player p, boolean reload){
        if(reload) reloadLayoutState(p);
        mainPage.open(p, getLayoutState(p), null);
    }

    public void openSettingsPage(Player p, Page previousPage){
        settingsPage.open(p, getLayoutState(p), previousPage);
    }

    private void reloadLayoutState(Player p){
        playerPageLayoutStates.put(p.getUniqueId(), plugin.getAuctionHouse().getDefaultLayoutBlocking(p));
    }

    private MainPageLayoutState getLayoutState(Player p){
        if(!playerPageLayoutStates.containsKey(p.getUniqueId())) {
            playerPageLayoutStates.put(p.getUniqueId(), plugin.getAuctionHouse().getDefaultLayoutBlocking(p));
        }
        return playerPageLayoutStates.get(p.getUniqueId());
    }

}
