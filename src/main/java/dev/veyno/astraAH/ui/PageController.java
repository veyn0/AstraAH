package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.ui.pages.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/*
TODO: move controlling last pages to this class. add e.g. stack to allow navigation back multiple pages.
 */

public class PageController {



    private final AstraAH plugin;
    private final UUID playerId;

    private MainPage mainPage;
    private SettingsPage settingsPage;
    private CreateListingsPage createListingsPage;
    private MyListingsPage myListingsPage;
    private ListingInfoPage listingInfoPage;

    public PageController(AstraAH plugin, UUID playerId) {
        this.plugin = plugin;
        this.playerId = playerId;
        createPages();
    }

    public void createPages(){
        mainPage = new MainPage(plugin, this, playerId, plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId)));
        settingsPage = new SettingsPage(plugin, this, playerId);
        createListingsPage = new CreateListingsPage(plugin, playerId, this);
        myListingsPage = new MyListingsPage(playerId, plugin, this);
        listingInfoPage = new ListingInfoPage(playerId, this, plugin);
    }

    public void openMainPage(boolean reload){
        if(reload) mainPage.rebuild();
        mainPage.open(null);
    }

    public void openSettingsPage(){
        settingsPage.rebuild();
        settingsPage.open(null);
    }

    public void openCreateListingsPage(){
        createListingsPage.open(null);
    }

    public MyListingsPage getMyListingsPage() {
        return myListingsPage;
    }

    public ListingInfoPage getListingInfoPage() {
        return listingInfoPage;
    }

    public MainPage getMainPage() {
        return mainPage;
    }

}
