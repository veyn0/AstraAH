package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.guis.PlayerListingsGuiConfiguration;
import dev.veyno.astraAH.data.dto.CachedListing;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MyListingsPage implements Page {

    private final AstraAH plugin;
    private final PageController pageController;
    private final UUID playerId;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion navBar;
    private ClickableInventory.InventoryRegion content;

    public MyListingsPage(AstraAH plugin, PageController pageController, UUID playerId) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerId = playerId;
        buildOnce();
    }

    @Override
    public void buildOnce() {
        PlayerListingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getPlayerListingsGuiConfiguration();
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerId));

        navBar = inventory.createRegionFromCoords("navigation", 0, 5, 8, 5);
        content = inventory.createRegionFromCoords("content", 0, 0, 8, 4);

        renderNavBar();
        renderContent();
    }

    @Override
    public void show() {
        inventory.open();
    }

    @Override
    public void reload() {
        renderNavBar();
        renderContent();
        navBar.refresh();
        content.refresh();
    }

    @Override
    public void invalidate(Section section) {
        switch (section) {
            case CONTENT -> { renderContent(); content.refresh(); }
            case NAVBAR  -> { renderNavBar();  navBar.refresh(); }
            case ALL     -> reload();
            default      -> { /* CATEGORIES, HISTORY don't exist here */ }
        }
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    private void renderContent() {
        content.clearItems();

        // TODO: replace with a dedicated per-seller lookup on ListingController
        //       (e.g. getListingsBySeller(UUID)) once it exists. The current
        //       implementation filters the full cached listing set client-side.

        List<Listing> listings = new ArrayList<>();
        for (CachedListing cached : plugin.getListingController().getListings()) {
            Listing l = cached.getListing();
            if (playerId.equals(l.getSellerId())) {
                listings.add(l);
            }
        }

        for (Listing l : listings) {
            content.addItem(
                    l.getContent(),
                    action -> pageController.openListingInfo(l)
            );
        }
    }

    private void renderNavBar() {
        PlayerListingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getPlayerListingsGuiConfiguration();

        navBar.setItem(
                0,
                configuration.getPreviousPageIcon(),
                action -> content.previousPageAndRefresh()
        );

        navBar.setItem(
                8,
                configuration.getNextPageIcon(),
                action -> content.nextPageAndRefresh()
        );

        navBar.setItem(
                2,
                configuration.getBackIcon(),
                action -> pageController.back()
        );

        navBar.setItem(
                4,
                configuration.getCreateListingIcon(),
                action -> pageController.openCreateListingsPage()
        );
    }
}