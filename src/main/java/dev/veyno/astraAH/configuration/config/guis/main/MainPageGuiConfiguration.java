package dev.veyno.astraAH.configuration.config.guis.main;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class MainPageGuiConfiguration extends Configurable {

    private final Component title;

    private String categoryEnabled;
    private String categoryDisabled;

    private final ItemStack navigationArrowLeft;
    private final ItemStack navigationArrowRight;
    private final ItemStack settingsIcon;
    private final ItemStack myListingsIcon;
    private final ItemStack refreshIcon;
    private final ItemStack sortIcon;
    private final ItemStack searchIcon;
    private final ItemStack categoriesIcon;
    private final ItemStack historyIcon;
    private final ItemStack scrollUpCategoriesIcon;
    private final ItemStack scrollDownCategoriesIcon;
    private final ItemStack scrollUpHistoryIcon;
    private final ItemStack scrollDownHistoryIcon;


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
        this.refreshIcon = getItem("refresh");
        this.sortIcon = getItem("sort");
        this.searchIcon = getItem("search");
        this.categoriesIcon = getItem("categories");
        this.historyIcon = getItem("history");
        this.scrollUpCategoriesIcon = getItem("scroll_up_categories");
        this.scrollDownCategoriesIcon = getItem("scroll_down_categories");
        this.scrollUpHistoryIcon = getItem("scroll_up_history");
        this.scrollDownHistoryIcon = getItem("scroll_down_history");
        this.categoryEnabled = getString("category_enabled");
        this.categoryDisabled = getString("category_disabled");
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

    public ItemStack getRefreshIcon() {
        return refreshIcon.clone();
    }

    public ItemStack getSortIcon() {
        return sortIcon.clone();
    }

    public ItemStack getSearchIcon() {
        return searchIcon.clone();
    }

    public ItemStack getCategoriesIcon() {
        return categoriesIcon.clone();
    }

    public ItemStack getHistoryIcon() {
        return historyIcon.clone();
    }

    public MainPageSortOptionsConfiguration getMainPageSortOptionsConfiguration() {
        return mainPageSortOptionsConfiguration;
    }

    public MainPageListingDisplayConfiguration getListingDisplayConfiguration() {
        return listingDisplayConfiguration;
    }

    public ItemStack getScrollUpCategoriesIcon() {
        return scrollUpCategoriesIcon;
    }

    public ItemStack getScrollDownCategoriesIcon() {
        return scrollDownCategoriesIcon;
    }

    public ItemStack getScrollUpHistoryIcon() {
        return scrollUpHistoryIcon;
    }

    public ItemStack getScrollDownHistoryIcon() {
        return scrollDownHistoryIcon;
    }


    public String getCategoryDisabled() {
        return categoryDisabled;
    }

    public String getCategoryEnabled() {
        return categoryEnabled;
    }
}
