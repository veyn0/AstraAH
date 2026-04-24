package dev.veyno.astraAH;

import dev.veyno.astraAH.ah.configuration.AstraAHConfiguration;
import dev.veyno.astraAH.app.ListingController;
import dev.veyno.astraAH.app.PlayerDataController;
import dev.veyno.astraAH.command.AuctionHouseCommand;
import dev.veyno.astraAH.data.ListingsService;
import dev.veyno.astraAH.data.PlayerDataService;
import dev.veyno.astraAH.data.repository.ListingsRepository;
import dev.veyno.astraAH.data.repository.PlayerDataRepository;
import dev.veyno.astraAH.data.repository.listings.YamlListingsRepository;
import dev.veyno.astraAH.data.repository.playerdata.YamlPlayerDataRepository;
import dev.veyno.astraAH.econ.EconomyProvider;
import dev.veyno.astraAH.econ.provider.FileEconomyProvider;
import dev.veyno.astraAH.permissions.PermissionsProvider;
import dev.veyno.astraAH.permissions.provider.DefaultPermissionsProvider;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*

TODO:
 - Improve storage occupation when
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
 - Backups: full backups or backups of partial stuff and actions, custom triggers where it can be specified what should be backed up
 - add pending deliveries for purchased items that did not fit in the inventory, optional enabled by default. add configurable maximum time an item is available to collect
 */

public final class AstraAH extends JavaPlugin {

    private AstraAHConfiguration configuration;

    private ListingController listingController;
    private PlayerDataController playerDataController;

    private Map<UUID, PageController> pageControllers = new ConcurrentHashMap<>();

    private ClickableInventory.InventoryManager inventoryManager;

    private EconomyProvider economy;

    private PermissionsProvider permissionsProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configuration = new AstraAHConfiguration(this);
        setupEconomy();
        setupCommands();

        ListingsRepository listingsRepository = new YamlListingsRepository(this, 30);
        this.listingController = new ListingController(
                this,
                new ListingsService(
                        this,
                        listingsRepository,
                        3000,
                        4000
                )
        );

        PlayerDataRepository playerDataRepository = new YamlPlayerDataRepository(this);
        this.playerDataController = new PlayerDataController(
                this,
                new PlayerDataService(
                        playerDataRepository,
                        this,
                        10
                )
        );

        inventoryManager = new ClickableInventory.InventoryManager(this);

        permissionsProvider = new DefaultPermissionsProvider();

    }

    public ListingController getListingController() {
        return listingController;
    }

    public PlayerDataController getPlayerDataController() {
        return playerDataController;
    }

    @Override
    public void onDisable() {
    }

    public ClickableInventory.InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public PermissionsProvider getPermissionsProvider() {
        return permissionsProvider;
    }

    public PageController getPageController(UUID playerId){
        return pageControllers.computeIfAbsent(
                playerId,
                playerId1 -> new PageController(this, playerId1)
        );
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
