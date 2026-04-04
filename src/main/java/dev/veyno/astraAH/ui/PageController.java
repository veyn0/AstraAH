package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.page.mainpage.MainPageLayoutState;
import dev.veyno.astraAH.ui.pages.MainPage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class PageController {

    private final AstraAH plugin;

    private MainPage mainPage;
    private Map<UUID, MainPageLayoutState> playerPageLayoutStates;

    public PageController(AstraAH plugin) {
        this.plugin = plugin;
        this.mainPage = new MainPage(plugin, this);
    }

    public void openMainPage(Player p){

        mainPage.open();



    }


}
