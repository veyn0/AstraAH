package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CategoryEditPage implements Page {

    private final AstraAH plugin;
    private UUID playerId;
    private final PageController pageController;

    public CategoryEditPage(PageController pageController, UUID playerId, AstraAH plugin) {
        this.pageController = pageController;
        this.playerId = playerId;
        this.plugin = plugin;
    }

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion content, navigation;

    @Override
    public void show() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void invalidate(Section section) {

    }

    @Override
    public void buildOnce() {
        inventory = new ClickableInventory(plugin.getInventoryManager(), Component.text("placeholder - edit categories") ,Bukkit.getPlayer(playerId));
        content = inventory.createRegionFromCoords("content",0,0,8,4);
        navigation = inventory.createRegionFromCoords("navbar", 0,5,8,5);
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    private void renderContent(){

    }

    private void renderNavBar(){

    }


}
