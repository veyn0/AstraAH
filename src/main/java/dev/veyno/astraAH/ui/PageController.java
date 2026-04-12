package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.ui.pages.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PageController {

    private final AstraAH plugin;

    private Map<UUID, MainPage> mainPages = new ConcurrentHashMap<>();

    private Map<UUID, SettingsPage> settingsPages = new ConcurrentHashMap<>();

    private Map<UUID, CreateListingsPage> createListingsPages = new ConcurrentHashMap<>();

    private Map<UUID, MyListingsPage> myListingsPages = new ConcurrentHashMap<>();

    private Map<UUID, ListingInfoPage> listingInfoPages = new ConcurrentHashMap<>();

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

    public void openCreateListingsPage(UUID playerID){
        CreateListingsPage page = getCreateListingsPage(playerID);
        page.open(getMyListingsPage(playerID));
    }


    public SettingsPage getSettingsPage(UUID playerId){
        if(!settingsPages.containsKey(playerId)) settingsPages.put(playerId, new SettingsPage(plugin, this, playerId) );
        return settingsPages.get(playerId);
    }

    public MainPage getMainPage(UUID playerId){
        if(!mainPages.containsKey(playerId)) mainPages.put(playerId, new MainPage(plugin, this, playerId, plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId))) );
        return mainPages.get(playerId);
    }

    public CreateListingsPage getCreateListingsPage(UUID playerID){
        if(!createListingsPages.containsKey(playerID)) createListingsPages.put(playerID, new CreateListingsPage(plugin, playerID, this));
        return createListingsPages.get(playerID);
    }

    public MyListingsPage getMyListingsPage(UUID playerID){
        if(!myListingsPages.containsKey(playerID)) myListingsPages.put(playerID, new MyListingsPage(playerID, plugin, this));
        return myListingsPages.get(playerID);
    }

    public ListingInfoPage getListingInfoPage(UUID playerID){
        if(!listingInfoPages.containsKey(playerID))listingInfoPages.put(playerID, new ListingInfoPage(playerID,this, plugin ));
        return listingInfoPages.get(playerID);
    }

}
