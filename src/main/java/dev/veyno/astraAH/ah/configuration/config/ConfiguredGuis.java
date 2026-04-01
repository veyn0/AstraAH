package dev.veyno.astraAH.ah.configuration.config;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import dev.veyno.astraAH.ah.configuration.config.guis.*;
import dev.veyno.astraAH.ah.configuration.config.guis.main.MainPageGuiConfiguration;

public class ConfiguredGuis extends Configurable {


    private CreateListingGuiConfiguration1 createListingGuiConfiguration1;
    private CreateListingGuiConfiguration2 createListingGuiConfiguration2;
    private CreateListingGuiConfiguration3 createListingGuiConfiguration3;
    private MainPageGuiConfiguration mainPageGuiConfiguration;
    private PlayerListingsGuiConfiguration playerListingsGuiConfiguration;
    private SettingsGuiConfiguration settingsGuiConfiguration;

    public ConfiguredGuis(AstraAH plugin, String path) {
        super(path, plugin);

        this.createListingGuiConfiguration1 = new CreateListingGuiConfiguration1(plugin, path + ".create_listing_1");
        this.createListingGuiConfiguration2 = new CreateListingGuiConfiguration2(path + ".create_listing_2", plugin);
        this.createListingGuiConfiguration3 = new CreateListingGuiConfiguration3(path + ".create_listing_3", plugin);
        this.mainPageGuiConfiguration = new MainPageGuiConfiguration(plugin, path + ".main_page");
        this.playerListingsGuiConfiguration = new PlayerListingsGuiConfiguration(path + ".my_listings", plugin);
        this.settingsGuiConfiguration = new SettingsGuiConfiguration(path + ".settings", plugin);

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
}
