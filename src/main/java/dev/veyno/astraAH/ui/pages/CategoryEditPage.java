package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.Category;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CategoryEditPage implements Page {

    private final AstraAH plugin;
    private UUID playerId;
    private final PageController pageController;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion content, navigation;

    public CategoryEditPage(PageController pageController, UUID playerId, AstraAH plugin) {
        this.pageController = pageController;
        this.playerId = playerId;
        this.plugin = plugin;
        buildOnce();
    }


    @Override
    public void show() {
        inventory.open();
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
        renderNavBar();
        renderContent();
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    private void renderContent(){
        PlayerData playerData = plugin.getPlayerDataController().getPlayerData(playerId);
        for(Category c : playerData.getPreferences().getCategories()){
            content.addItem(
                    c.getPreview(),
                    action -> {

                    }
            );

        }
    }

    private void renderNavBar(){
        navigation.setItem(
                0,
                new ItemStack(Material.ARROW),
                action -> {
                    content.previousPageAndRefresh();
                }
        );
        navigation.setItem(
                2,
                new ItemStack(Material.SPECTRAL_ARROW),
                action -> {
                    pageController.back();
                }
        );
        navigation.setItem(
                4,
                new ItemStack(Material.FURNACE),
                action -> {

                }
        );
        navigation.setItem(
                8,
                new ItemStack(Material.ARROW),
                action -> {
                    content.nextPageAndRefresh();
                }
        );
    }

}
