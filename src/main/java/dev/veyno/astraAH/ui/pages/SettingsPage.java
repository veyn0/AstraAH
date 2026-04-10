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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SettingsPage implements Page {

    private final AstraAH plugin;

    private final PageController pageController;

    public SettingsPage(AstraAH plugin, PageController pageController) {
        this.plugin = plugin;
        this.pageController = pageController;
    }

    @Override
    public void open(Player p, MainPageLayoutState state, Page previousPage) {
        Bukkit.getLogger().info("opening settings");
        SettingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getSettingsGuiConfiguration();
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), p);
        ClickableInventory.InventoryRegion content = inventory.createRegionFromCoords("content", 0,0, 8,5);

        content.addItem(
                configuration.getCategoryToggleIcon(),
                action ->{
                    Bukkit.getLogger().info("clicked toggle categories");
                    MainPageLayoutState.ButtonLayout layout = state.getAdvancedCategories();
                    if(layout== MainPageLayoutState.ButtonLayout.DISABLED){
                    }
                    else {
                        PlayerPreferences preferences = plugin.getAuctionHouse().getPreferencesBlocking(p.getUniqueId());
                        Bukkit.getLogger().info("Categories: " + preferences.showCategories());
                        plugin.getAuctionHouse().setPreferencesBlocking(p.getUniqueId(), new PlayerPreferences(p.getUniqueId(), preferences.categoryEntries(), !preferences.showCategories(), preferences.showHistory() ));
                    }
                }
        );

        content.addItem(
                new ItemStack(Material.ARROW),
                actio ->{
                    pageController.openMainPage(p, true);
                }
        );

        inventory.open();
    }

    @Override
    public Component getPageTitle() {
        return null;
    }
}
