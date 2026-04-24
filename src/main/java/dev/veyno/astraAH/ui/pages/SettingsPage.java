package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.config.guis.SettingsGuiConfiguration;
import dev.veyno.astraAH.app.PlayerDataManager;
import dev.veyno.astraAH.app.dto.ButtonLayout;
import dev.veyno.astraAH.app.dto.LayoutTemplate;
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
    private final UUID playerId;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion content;

    public SettingsPage(AstraAH plugin, PageController pageController, UUID playerId) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerId = playerId;
        buildOnce();
    }

    @Override
    public void buildOnce() {
        SettingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getSettingsGuiConfiguration();
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerId));
        content = inventory.createRegionFromCoords("content", 0, 0, 8, 5);
        renderContent();
    }

    @Override
    public void show() {
        inventory.open();
    }

    @Override
    public void reload() {
        renderContent();
        content.refresh();
    }

    @Override
    public void invalidate(Section section) {
        if (section == Section.CONTENT || section == Section.ALL) {
            renderContent();
            content.refresh();
        }
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    private void renderContent() {
        SettingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getSettingsGuiConfiguration();
        content.clearItems();

        content.setItem(
                10,
                configuration.getCategoryToggleIcon(),
                action -> onToggleCategories()
        );

        content.setItem(
                12,
                configuration.getHistoryToggleIcon(),
                action -> onToggleHistory()
        );

        content.setItem(
                45,
                new ItemStack(Material.ARROW),
                action -> pageController.back()
        );
    }

    private void onToggleCategories() {
        LayoutTemplate layoutTemplate = plugin.getPlayerDataController().getLayoutTemplate(Bukkit.getPlayer(playerId));
        if (layoutTemplate.getAdvancedCategories() == ButtonLayout.DISABLED) return;

        PlayerDataManager manager = plugin.getPlayerDataController().getPlayerDataManager();
        manager.getPreferencesManager(playerId).toggleShowCategories();

        pageController.getMainPage().reload();
    }

    private void onToggleHistory() {
        LayoutTemplate layoutTemplate = plugin.getPlayerDataController().getLayoutTemplate(Bukkit.getPlayer(playerId));
        if (layoutTemplate.getAdvancedHistory() == ButtonLayout.DISABLED) return;

        PlayerDataManager manager = plugin.getPlayerDataController().getPlayerDataManager();
        manager.getPreferencesManager(playerId).toggleShowHistory();

        pageController.getMainPage().reload();
    }
}