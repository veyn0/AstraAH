package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.ui.pages.*;
import org.bukkit.Bukkit;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class PageController {

    private final AstraAH plugin;
    private final UUID playerId;

    private final MainPage mainPage;
    private final SettingsPage settingsPage;
    private final CreateListingsPage createListingsPage;
    private final MyListingsPage myListingsPage;
    private final ListingInfoPage listingInfoPage;
    private final CategoryEditPage categoryEditPage;

    private final Deque<Page> history = new ArrayDeque<>();

    private Page current;

    public PageController(AstraAH plugin, UUID playerId) {
        this.plugin = plugin;
        this.playerId = playerId;

        this.mainPage = new MainPage(plugin, this, playerId);
        this.settingsPage = new SettingsPage(plugin, this, playerId);
        this.createListingsPage = new CreateListingsPage(plugin, this, playerId);
        this.myListingsPage = new MyListingsPage(plugin, this, playerId);
        this.listingInfoPage = new ListingInfoPage(plugin, this, playerId);
        this.categoryEditPage = new CategoryEditPage(this, playerId,plugin );
    }

    public void navigate(Page target) {
        if (target == null) return;
        if (current != null && current != target) {
            history.push(current);
        }
        current = target;
        target.show();
    }

    public void back() {
        if (history.isEmpty()) return;
        current = history.pop();
        current.show();
    }

    public void openCategoryEditPage(){
        navigate(categoryEditPage);
    }

    /**
     * User-driven reload of whatever page is currently visible. This is
     * the only path through which a full page rebuild is triggered from
     * outside the page itself.
     */
    public void reloadCurrent() {
        if (current != null) current.reload();
    }

    public Page getCurrent() {
        return current;
    }

    public boolean canGoBack() {
        return !history.isEmpty();
    }

    public void openMainPage() {
        navigate(mainPage);
    }

    public void openSettingsPage() {
        navigate(settingsPage);
    }

    public void openMyListingsPage() {
        navigate(myListingsPage);
    }


    public void openCreateListingsPage() {
        createListingsPage.reload();
        navigate(createListingsPage);
    }

    public void openListingInfo(Listing listing) {
        listingInfoPage.setListing(listing);
        navigate(listingInfoPage);
    }

    public MainPage getMainPage()                   { return mainPage; }
    public SettingsPage getSettingsPage()           { return settingsPage; }
    public CreateListingsPage getCreateListingsPage(){ return createListingsPage; }
    public MyListingsPage getMyListingsPage()       { return myListingsPage; }
    public ListingInfoPage getListingInfoPage()     { return listingInfoPage; }

    public UUID getPlayerId() {
        return playerId;
    }
}