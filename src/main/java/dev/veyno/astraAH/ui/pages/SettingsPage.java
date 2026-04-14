package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.guis.SettingsGuiConfiguration;
import dev.veyno.astraAH.entity.PlayerPreferences;
import dev.veyno.astraAH.entity.page.mainpage.MainPageLayoutState;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SettingsPage implements Page {

    private final AstraAH plugin;

    private final PageController pageController;

    private UUID playerId;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion content;

    public SettingsPage(AstraAH plugin, PageController pageController, UUID playerId) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerId = playerId;
    }

    @Override
    public void open(Page previousPage) {
        inventory.open();
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    @Override
    public void rebuild() {
        SettingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getSettingsGuiConfiguration();
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerId));
        content = inventory.createRegionFromCoords("content", 0,0, 8,5);

//        AllowedPlayerActions allowedPlayerActions = plugin.getAuctionHouse().getAllowedPlayerActionsBlocking(playerId);

        content.setItem(
                10,
                configuration.getCategoryToggleIcon(),
                action ->{
                    MainPageLayoutState.ButtonLayout layout = plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId)).getAdvancedCategories();
                    if(layout!= MainPageLayoutState.ButtonLayout.DISABLED){
                        PlayerPreferences preferences = plugin.getAuctionHouse().getPreferencesBlocking(playerId);
                        plugin.getAuctionHouse().setPreferencesBlocking(playerId, new PlayerPreferences(playerId, preferences.categoryEntries(), !preferences.showCategories(), preferences.showHistory() ));
                        MainPage mainPage = pageController.getMainPage();
                        mainPage.setLayoutState(plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId)));
                        mainPage.resetFilter();
                        mainPage.rebuild();
                    }
                }
        );

        content.setItem(
                12,
                configuration.getHistoryToggleIcon(),
                action ->{
                    MainPageLayoutState.ButtonLayout layout = plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId)).getAdvancedHistory();
                    if(layout!= MainPageLayoutState.ButtonLayout.DISABLED){
                        PlayerPreferences preferences = plugin.getAuctionHouse().getPreferencesBlocking(playerId);
                        plugin.getAuctionHouse().setPreferencesBlocking(playerId, new PlayerPreferences(playerId, preferences.categoryEntries(), preferences.showCategories(), !preferences.showHistory() ));
                        MainPage mainPage = pageController.getMainPage();
                        mainPage.setLayoutState(plugin.getAuctionHouse().getLayoutBlocking(Bukkit.getPlayer(playerId)));
                        mainPage.resetFilter();
                        mainPage.rebuild();
                    }
                }
        );

        content.setItem(
                45,
                new ItemStack(Material.ARROW),
                actio ->{
                    pageController.openMainPage(true);
                }
        );

    }

    @Override
    public void refresh() {

    }
}
