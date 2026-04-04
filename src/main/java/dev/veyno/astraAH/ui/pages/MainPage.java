package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.SortType;
import dev.veyno.astraAH.ah.configuration.config.guis.main.MainPageGuiConfiguration;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.entity.ui.MainPageLayoutState;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class MainPage implements Page {

    private final AstraAH plugin;

    private final PageController pageController;

    public MainPage(AstraAH plugin, PageController pageController) {
        this.plugin = plugin;
        this.pageController = pageController;
    }

    @Override
    public void open(Player p, MainPageLayoutState state, Page previousPage) {
        MainPageGuiConfiguration mainPageGuiConfiguration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();

        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), mainPageGuiConfiguration.getTitle(), p);

        ClickableInventory.InventoryRegion centerContent = buildCenterContent(p, state);
    }

    @Override
    public Component getPageTitle() {
        return plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getTitle();
    }

    private ClickableInventory.InventoryRegion buildCenterContent(Player p, MainPageLayoutState layoutState){
        int fromX = layoutState.isShowAdvancedCategories() ? 1 : 0;
        int fromY = 0;
        int toX = layoutState.isShowAdvancedHistory() ? 7 : 8;
        int toY = 4;

        int page = layoutState.getListingsPageIndex();

        List<Listing> listings = sortListings(plugin.getAuctionHouse().getListings(), layoutState.getSortType());

        return null;
    }

    public List<Listing> sortListings(List<Listing> listings, SortType sortType) {
        if(sortType==null) return listings;
        return switch (sortType) {
            case NAME_A_Z -> listings.stream()
                    .sorted(Comparator.comparing(l -> l.content().getType().name()))
                    .toList();

            case NAME_Z_A -> listings.stream()
                    .sorted(Comparator.comparing((Listing l) -> l.content().getType().name()).reversed())
                    .toList();

            case PRICE_H_L -> listings.stream()
                    .sorted(Comparator.comparingDouble(Listing::price).reversed())
                    .toList();

            case PRICE_L_H -> listings.stream()
                    .sorted(Comparator.comparingDouble(Listing::price))
                    .toList();

            case PRICE_PER_PIECE_H_L -> listings.stream()
                    .sorted(Comparator.comparingDouble((Listing l) ->
                            -(l.price() / l.content().getAmount()))
                    )
                    .toList();

            case PRICE_PER_PICE_L_H -> listings.stream()
                    .sorted(Comparator.comparingDouble(l ->
                            l.price() / l.content().getAmount())
                    )
                    .toList();
        };
    }


}
