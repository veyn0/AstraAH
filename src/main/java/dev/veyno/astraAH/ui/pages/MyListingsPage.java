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

    private UUID playerID;
    private AstraAH plugin;
    private PageController pageController;

    private ClickableInventory overviewInventory;

    private ClickableInventory.InventoryRegion navBar;
    private ClickableInventory.InventoryRegion content;

    public MyListingsPage(UUID playerID, AstraAH plugin, PageController pageController) {
        this.playerID = playerID;
        this.plugin = plugin;
        this.pageController = pageController;
        rebuild();
    }

    @Override
    public void open(Page previousPage) {
        overviewInventory.open();
    }

    @Override
    public Component getPageTitle() {
        return overviewInventory.getTitle();
    }

    @Override
    public void rebuild() {
        PlayerListingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getPlayerListingsGuiConfiguration();
        overviewInventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerID));

        navBar = overviewInventory.createRegionFromCoords("navigation", 0, 5, 8, 5);
        buildNavBar();

        content = overviewInventory.createRegionFromCoords("content", 0, 0, 8, 4);
        buildContent();
    }

    @Override
    public void refresh() {

    }

    private void buildContent() {
        // TODO: replace with a dedicated per-seller lookup on ListingController (e.g. getListingsBySeller(UUID))
        //       once it exists. Current implementation filters the full cached listing set client-side.
        List<Listing> listings = new ArrayList<>();
        for (CachedListing cached : plugin.getListingController().getListings()) {
            Listing l = cached.getListing();
            if (playerID.equals(l.getSellerId())) {
                listings.add(l);
            }
        }

        content.clearItems();

        for (Listing l : listings) {
            content.addItem(
                    l.getContent(),
                    action -> {
                        pageController.getListingInfoPage().openListingInfo(l, this);
                    }
            );
        }

    }

    private void buildNavBar() {
        PlayerListingsGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getPlayerListingsGuiConfiguration();

        navBar.setItem(
                0,
                configuration.getPreviousPageIcon(),
                action -> {
                    content.previousPageAndRefresh();
                }
        );

        navBar.setItem(
                8,
                configuration.getNextPageIcon(),
                action -> {
                    content.nextPageAndRefresh();
                }
        );

        navBar.setItem(
                2,
                configuration.getBackIcon(),
                action -> {
                    pageController.openMainPage(false);
                }
        );

        navBar.setItem(
                4,
                configuration.getCreateListingIcon(),
                action -> {
                    pageController.openCreateListingsPage();
                }
        );
    }
}