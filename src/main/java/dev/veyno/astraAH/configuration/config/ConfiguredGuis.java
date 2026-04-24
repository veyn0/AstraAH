package dev.veyno.astraAH.configuration.config;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import dev.veyno.astraAH.configuration.config.guis.*;
import dev.veyno.astraAH.configuration.config.guis.main.MainPageGuiConfiguration;

public class ConfiguredGuis extends Configurable {


    private final CreateListingGuiConfiguration1 createListingGuiConfiguration1;
    private final CreateListingGuiConfiguration2 createListingGuiConfiguration2;
    private final CreateListingGuiConfiguration3 createListingGuiConfiguration3;
    private final MainPageGuiConfiguration mainPageGuiConfiguration;
    private final PlayerListingsGuiConfiguration playerListingsGuiConfiguration;
    private final SettingsGuiConfiguration settingsGuiConfiguration;
    private final ListingInfoGuiConfiguration listingInfoGuiConfiguration;

    public ConfiguredGuis(AstraAH plugin, String path) {
        super(path, plugin);

        this.createListingGuiConfiguration1 = new CreateListingGuiConfiguration1(plugin, path + ".create_listing_1");
        this.createListingGuiConfiguration2 = new CreateListingGuiConfiguration2(path + ".create_listing_2", plugin);
        this.createListingGuiConfiguration3 = new CreateListingGuiConfiguration3(path + ".create_listing_3", plugin);
        this.mainPageGuiConfiguration = new MainPageGuiConfiguration(plugin, path + ".main_page");
        this.playerListingsGuiConfiguration = new PlayerListingsGuiConfiguration(path + ".my_listings", plugin);
        this.settingsGuiConfiguration = new SettingsGuiConfiguration(path + ".settings", plugin);
        this.listingInfoGuiConfiguration = new ListingInfoGuiConfiguration(plugin, path + ".listing_info");

    }

    public CreateListingGuiConfiguration1 getCreateListingGuiConfiguration1() {
        return createListingGuiConfiguration1;
    }

    public CreateListingGuiConfiguration2 getCreateListingGuiConfiguration2() {
        return createListingGuiConfiguration2;
    }

    public CreateListingGuiConfiguration3 getCreateListingGuiConfiguration3() {
        return createListingGuiConfiguration3;
    }

    public MainPageGuiConfiguration getMainPageGuiConfiguration() {
        return mainPageGuiConfiguration;
    }

    public PlayerListingsGuiConfiguration getPlayerListingsGuiConfiguration() {
        return playerListingsGuiConfiguration;
    }

    public SettingsGuiConfiguration getSettingsGuiConfiguration() {
        return settingsGuiConfiguration;
    }

    public ListingInfoGuiConfiguration getListingInfoGuiConfiguration() {
        return listingInfoGuiConfiguration;
    }
}
