package dev.veyno.astraAH;

import dev.veyno.astraAH.ah.AuctionHouse;
import dev.veyno.astraAH.command.AuctionHouseCommand;
import dev.veyno.astraAH.econ.EconomyProvider;
import dev.veyno.astraAH.econ.provider.FileEconomyProvider;
import dev.veyno.astraAH.econ.provider.VaultEconomyProvider;
import dev.veyno.astraAH.storage.StorageProvider;
import dev.veyno.astraAH.storage.provider.FileStorageProvider;
import dev.veyno.astraAH.ui.error.ErrorHandler;
import dev.veyno.astraAH.ui.UIController;
import dev.veyno.astraAH.util.ClickableInventory;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;


/*

TODO:
 - Exclude specific items, including being able to match names

 */

public final class AstraAH extends JavaPlugin {

    private StorageProvider listingStorage;

    private UIController uiController;

    private ErrorHandler errorHandler;

    private ClickableInventory.InventoryManager inventoryManager;

    private AuctionHouse auctionHouse;

    private EconomyProvider economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupEconomy();
        setupCommands();
        listingStorage = new FileStorageProvider(this);
        auctionHouse = new AuctionHouse(this, listingStorage, economy);
        inventoryManager = new ClickableInventory.InventoryManager(this);
        errorHandler = new ErrorHandler(this);
        uiController = new UIController(this);
    }

    @Override
    public void onDisable() {
    }

    public ClickableInventory.InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public UIController getUiController() {
        return uiController;
    }

    public AuctionHouse getAuctionHouse() {
        return auctionHouse;
    }

    public StorageProvider getListingStorage() {
        return listingStorage;
    }

    private void setupEconomy(){

        //TODO: implement more economy providers and the selection in the config file aswell as automaticly using the file to provide economy
        try {
            //economy = VaultEconomyProvider.createOrNull();
        }catch (Exception e){
            getLogger().warning("Vault API not found");
        }
        if(economy==null){
            economy= new FileEconomyProvider(this);
        }
    }

    private void setupCommands(){
        getCommand("market").setExecutor(new AuctionHouseCommand(this));
    }
}
