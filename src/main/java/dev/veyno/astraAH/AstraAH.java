package dev.veyno.astraAH;

//import de.leycm.i18label4j.CommonLabelProvider;
//import de.leycm.i18label4j.Label;
//import de.leycm.i18label4j.LabelProvider;
//import de.leycm.i18label4j.mapping.MappingRule;
//import de.leycm.i18label4j.serializer.KyoriAdventureSerializer;
//import de.leycm.i18label4j.source.FileSource;
//import de.leycm.init4j.instance.Instanceable;

import dev.veyno.astraAH.ah.AuctionHouse;
import dev.veyno.astraAH.ah.configuration.AstraAHConfiguration;
import dev.veyno.astraAH.command.AuctionHouseCommand;
import dev.veyno.astraAH.econ.EconomyProvider;
import dev.veyno.astraAH.econ.provider.FileEconomyProvider;
import dev.veyno.astraAH.permissions.PermissionsProvider;
import dev.veyno.astraAH.permissions.provider.DefaultPermissionsProvider;
import dev.veyno.astraAH.storage.actions.AHPlayerActionsStorageProvider;
import dev.veyno.astraAH.storage.actions.provider.FileAHPlayerActionsStorageProvider;
import dev.veyno.astraAH.storage.history.AHTransactionHistoryStorageProvider;
import dev.veyno.astraAH.storage.history.provider.FileAHTransactionHistoryStorageProvider;
import dev.veyno.astraAH.storage.listings.AHListingsStorageProvider;
import dev.veyno.astraAH.storage.listings.provider.FileAHListingsStorageProvider;
import dev.veyno.astraAH.storage.preferences.AHPlayerPreferencesStorageProvider;
import dev.veyno.astraAH.storage.preferences.provider.FileAHPlayerPreferencesStorageProvider;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.ui.error.ErrorHandler;
import dev.veyno.astraAH.ui.UIController;
import dev.veyno.astraAH.util.ClickableInventory;
import org.bukkit.plugin.java.JavaPlugin;

/*

TODO:
 - Exclude specific items, including being able to match names
 - save transaction history in transactions.yml and
 - add quick filter section at right side of screen based on the last 4 items that were purchased. click -> filter by material and lowest price automatically
 - create custom filters per user, individual preferences such as default currency, category or sorting type
 - multi currency support
 - configurable fees/taxes e.g. base-fee + X% tax, both configurable if to pay upfront or not
 - API
 - Register plugin as independent economy provider if none present via vault
 - Admin UI: transaction history, per player, stats e.g. total money made
 - alerts: thresholds /h for money made, specific interactions and all time values.
 - add customizable filters: when joining initially player filters will be set to the default filters and they can edit them afterwards

 */

public final class AstraAH extends JavaPlugin {

    private AstraAHConfiguration configuration;

    private AHListingsStorageProvider storageProvider;
    private AHPlayerPreferencesStorageProvider playerPreferencesStorageProvider;
    private AHTransactionHistoryStorageProvider transactionHistoryStorageProvider;
    private AHPlayerActionsStorageProvider playerActionsStorageProvider;

    private PageController pageController;

    private ErrorHandler errorHandler;

    private ClickableInventory.InventoryManager inventoryManager;

    private AuctionHouse auctionHouse;

    private EconomyProvider economy;

    private PermissionsProvider permissionsProvider;

//    private LabelProvider provider;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configuration = new AstraAHConfiguration(this);
        setupEconomy();
        setupCommands();
       // saveResource("lang/en_us.yml", false);
        storageProvider = new FileAHListingsStorageProvider(this);
        playerPreferencesStorageProvider = new FileAHPlayerPreferencesStorageProvider(this);
        transactionHistoryStorageProvider = new FileAHTransactionHistoryStorageProvider(this);
        playerActionsStorageProvider = new FileAHPlayerActionsStorageProvider(this);
        auctionHouse = new AuctionHouse(
                this,
                storageProvider,
                playerPreferencesStorageProvider,
                transactionHistoryStorageProvider,
                playerActionsStorageProvider,
                economy
        );
        inventoryManager = new ClickableInventory.InventoryManager(this);
        errorHandler = new ErrorHandler(this);

        permissionsProvider = new DefaultPermissionsProvider();

        pageController = new PageController(this);


//        FileSource source = FileSource.yaml(new File(getDataFolder(), "lang").toURI());
//        provider = CommonLabelProvider.builder()
//                .defaultMappingRule(MappingRule.CURLY)
//                .withSerializer(Component.class, new KyoriAdventureSerializer.KyoriMiniMessage())
//                .buildWarm(source, Locale.US);
//
//        Instanceable.register(provider, LabelProvider.class);
//
//
//        Label.of("w").map("pl1", "<red>du stinkst</red>").resolve(Component.class);

//        for(int i = 0; i < 10; i++) {
//            for (Listing l : UIController.createExampleListings()) {
//
//                try {
//                    listingStorage.saveListing(l);
//                } catch (Exception e) {
//                }
//            }
//        }

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

    public PageController getPageController() {
        return pageController;
    }

    public AuctionHouse getAuctionHouse() {
        return auctionHouse;
    }

    public PermissionsProvider getPermissionsProvider() {
        return permissionsProvider;
    }

    public AHListingsStorageProvider getStorageProvider() {
        return storageProvider;
    }

    public AHPlayerPreferencesStorageProvider getPlayerPreferencesStorageProvider() {
        return playerPreferencesStorageProvider;
    }

    public AHTransactionHistoryStorageProvider getTransactionHistoryStorageProvider() {
        return transactionHistoryStorageProvider;
    }

    public AHPlayerActionsStorageProvider getPlayerActionsStorageProvider() {
        return playerActionsStorageProvider;
    }

    public AstraAHConfiguration getConfiguration() {
        return configuration;
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
