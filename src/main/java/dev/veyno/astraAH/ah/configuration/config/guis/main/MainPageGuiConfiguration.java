package dev.veyno.astraAH.ah.configuration.config.guis.main;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class MainPageGuiConfiguration extends Configurable {

    private final Component title;

    private final ItemStack navigationArrowLeft;
    private final ItemStack navigationArrowRight;
    private final ItemStack settingsIcon;
    private final ItemStack myListingsIcon;
    private final ItemStack createListingIcon;
    private final ItemStack sortIcon;
    private final ItemStack searchIcon;


    private final MainPageSortOptionsConfiguration mainPageSortOptionsConfiguration;
    private final MainPageListingDisplayConfiguration listingDisplayConfiguration;


    public MainPageGuiConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.mainPageSortOptionsConfiguration = new MainPageSortOptionsConfiguration(path + ".sort_options", plugin);
        this.listingDisplayConfiguration = new MainPageListingDisplayConfiguration(plugin, path + ".items");

        this.title = getMessage("title");

        this.navigationArrowLeft = getItem("arrow_left");
        this.navigationArrowRight = getItem("arrow_right");
        this.settingsIcon = getItem("settings");
        this.myListingsIcon = getItem("my_listings");
        this.createListingIcon = getItem("create_listing");
        this.sortIcon = getItem("sort");
        this.searchIcon = getItem("search");
    }

    public Component getTitle() {
        return title;
    }

    public ItemStack getNavigationArrowLeft() {
        return navigationArrowLeft.clone();
    }

    public ItemStack getNavigationArrowRight() {
        return navigationArrowRight.clone();
    }

    public ItemStack getSettingsIcon() {
        return settingsIcon.clone();
    }

    public ItemStack getMyListingsIcon() {
        return myListingsIcon.clone();
    }

    public ItemStack getCreateListingIcon() {
        return createListingIcon.clone();
    }

    public ItemStack getSortIcon() {
        return sortIcon.clone();
    }

    public ItemStack getSearchIcon() {
        return searchIcon.clone();
    }

    public MainPageSortOptionsConfiguration getMainPageSortOptionsConfiguration() {
        return mainPageSortOptionsConfiguration;
    }

    public MainPageListingDisplayConfiguration getListingDisplayConfiguration() {
        return listingDisplayConfiguration;
    }
}
