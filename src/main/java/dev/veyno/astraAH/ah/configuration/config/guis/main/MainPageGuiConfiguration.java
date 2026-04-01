package dev.veyno.astraAH.ah.configuration.config.guis.main;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class MainPageGuiConfiguration extends Configurable {

    private Component title;

    private ItemStack navigationArrowLeft;
    private ItemStack navigationArrowRight;
    private ItemStack settingsIcon;
    private ItemStack myListingsIcon;
    private ItemStack createListingIcon;
    private ItemStack sortIcon;
    private ItemStack searchIcon;


    private MainPageSortOptionsConfiguration mainpageSortOptionsConfiguration;


    public MainPageGuiConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        mainpageSortOptionsConfiguration = new MainPageSortOptionsConfiguration(path + ".sort_options", plugin);

        title = getMessage("title");

        navigationArrowLeft = getItem("arrow_left");
        navigationArrowRight = getItem("arrow_right");
        settingsIcon = getItem("settings");
        myListingsIcon = getItem("my_listings");
        createListingIcon = getItem("create_listing");
        sortIcon = getItem("sort");
        searchIcon = getItem("search");
    }

}
