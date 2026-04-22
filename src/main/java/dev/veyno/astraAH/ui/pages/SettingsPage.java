package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.guis.SettingsGuiConfiguration;
import dev.veyno.astraAH.app.dto.ButtonLayout;
import dev.veyno.astraAH.app.dto.LayoutTemplate;
import dev.veyno.astraAH.data.dto.Preferences;
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
        content = inventory.createRegionFromCoords("content", 0, 0, 8, 5);

        content.setItem(
                10,
                configuration.getCategoryToggleIcon(),
                action -> {
                    // TODO: replace with future controller method that returns LayoutTemplate for a player
                    LayoutTemplate layoutTemplate = null; // plugin.getXxxController().getLayoutTemplate(playerId);
                    if (layoutTemplate == null) return;
                    ButtonLayout layout = layoutTemplate.getAdvancedCategories();
                    if (layout != ButtonLayout.DISABLED) {
                        Preferences preferences = plugin.getPlayerDataController().getPlayerData(playerId).getPreferences();
                        // TODO: toggle showCategories once PlayerDataFacade (mutate/save) exists.
                        // Intended behaviour:
                        //   playerDataFacade.toggleShowCategories(playerId);
                        //   MainPage mainPage = pageController.getMainPage();
                        //   mainPage.setLayoutState(plugin.getXxxController().getLayoutTemplate(playerId));
                        //   mainPage.resetFilter();
                        //   mainPage.rebuild();
                    }
                }
        );

        content.setItem(
                12,
                configuration.getHistoryToggleIcon(),
                action -> {
                    // TODO: replace with future controller method that returns LayoutTemplate for a player
                    LayoutTemplate layoutTemplate = null; // plugin.getXxxController().getLayoutTemplate(playerId);
                    if (layoutTemplate == null) return;
                    ButtonLayout layout = layoutTemplate.getAdvancedHistory();
                    if (layout != ButtonLayout.DISABLED) {
                        Preferences preferences = plugin.getPlayerDataController().getPlayerData(playerId).getPreferences();
                        // TODO: toggle showHistory once PlayerDataFacade (mutate/save) exists.
                        // Intended behaviour:
                        //   playerDataFacade.toggleShowHistory(playerId);
                        //   MainPage mainPage = pageController.getMainPage();
                        //   mainPage.setLayoutState(plugin.getXxxController().getLayoutTemplate(playerId));
                        //   mainPage.resetFilter();
                        //   mainPage.rebuild();
                    }
                }
        );

        content.setItem(
                45,
                new ItemStack(Material.ARROW),
                actio -> {
                    pageController.openMainPage(true);
                }
        );

    }

    @Override
    public void refresh() {

    }
}