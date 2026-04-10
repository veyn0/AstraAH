package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.page.mainpage.MainPageLayoutState;
import dev.veyno.astraAH.ui.pages.MainPage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PageController {

    private final AstraAH plugin;

    private MainPage mainPage;
    private Map<UUID, MainPageLayoutState> playerPageLayoutStates = new ConcurrentHashMap<>();

    public PageController(AstraAH plugin) {
        this.plugin = plugin;
        this.mainPage = new MainPage(plugin, this);
    }

    public void openMainPage(Player p){

        if(!playerPageLayoutStates.containsKey(p.getUniqueId())) {
            playerPageLayoutStates.put(p.getUniqueId(), plugin.getAuctionHouse().getDefaultLayoutBlocking(p));
        }
        mainPage.open(p, playerPageLayoutStates.get(p.getUniqueId()), null);



    }


}
