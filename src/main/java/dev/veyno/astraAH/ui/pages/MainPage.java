package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.SortType;
import dev.veyno.astraAH.ah.configuration.config.guis.main.MainPageGuiConfiguration;
import dev.veyno.astraAH.entity.AHTransactionHistoryEntry;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.entity.PlayerPreferences;
import dev.veyno.astraAH.entity.PlayerPreferencesCategoryEntry;
import dev.veyno.astraAH.entity.page.mainpage.MainPageLayoutState;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MainPage implements Page {

    /*
    TODO: pro spieler instanzieren, verwerfen das nicht persistente dinge in layoutstate gespeichert werden, alles soll auf basis des Clickableinventory objekts passieren und nciht doppelt gespeichert werden.
     */

    private final AstraAH plugin;

    private final PageController pageController;

    private List<Material> filter;

    private UUID playerID;

    private int categoryItemSelectedIndex = 0;
    private int historySelectedIndex = 0;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion center;
    private ClickableInventory.InventoryRegion sidebarLeft;
    private ClickableInventory.InventoryRegion sidebarRight;
    private ClickableInventory.InventoryRegion bottom;

    private MainPageLayoutState layoutState;

    private MainPageGuiConfiguration configuration;

    public MainPageLayoutState getLayoutState() {
        return layoutState;
    }

    public MainPage(AstraAH plugin, PageController pageController, UUID playerID, MainPageLayoutState layoutState) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerID = playerID;
        this.configuration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();
        this.layoutState = layoutState;
        rebuild();
    }

    @Override
    public void open(Page previousPage) {
        inventory.open();
    }

    @Override
    public Component getPageTitle() {
        return configuration.getTitle();
    }

    @Override
    public void rebuild() {
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerID) );
        createNavbar();
        buildCenterContent();
        if(layoutState.getAdvancedCategories()== MainPageLayoutState.ButtonLayout.SIDEBAR) {
            buildCategorySidebar();
        }
    }

    @Override
    public void refresh() {
        //TODO
    }

    private void buildCategorySidebar(){
        sidebarLeft = inventory.createRegionFromCoords("categories", 0, 0, 0,5);

        sidebarLeft.setStaticItem(
                0,
                new ItemStack(Material.SPECTRAL_ARROW),
                action ->{
                    if(!action.isLeftClick()) return;
                    sidebarLeft.scrollByAndRefresh(-1);
                }
        );

        sidebarLeft.setStaticItem(
                5,
                new ItemStack(Material.SPECTRAL_ARROW),
                action ->{
                    if(!action.isLeftClick()) return;
                    sidebarLeft.scrollByAndRefresh(1);
                }
        );

        for(PlayerPreferencesCategoryEntry entry : plugin.getAuctionHouse().getPreferencesBlocking(playerID).categoryEntries()){
            sidebarLeft.addItem(
                    entry.preview(),
                    action ->{
                        if(!action.isLeftClick()) return;
                        filter = entry.filter();
                        buildCenterContent();
                        center.refresh();
                        //open(Bukkit.getPlayer(playerID), layoutState, null);
                    }
            );
        }
    }

    private void buildCenterContent(){
        int fromX = layoutState.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.SIDEBAR ? 1 : 0;
        int fromY = 0;
        int toX = layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.SIDEBAR ? 7 : 8;
        int toY = 4;

        List<Listing> listings = sortListings(plugin.getAuctionHouse().getListings(), layoutState.getSortType());

        if(center!=null) inventory.removeRegion("center");
        center = inventory.createRegionFromCoords("center", fromX, fromY, toX, toY);

        for(Listing l : listings){
            if(filter != null && !filter.contains(l.content().getType())) continue;
            center.addItem(getDisplayItem(l), clickContext -> {
                if(clickContext.isLeftClick()){
                    plugin.getLogger().info( Bukkit.getPlayer(playerID).getName()+" Leftclicked Listing.");
                }
                else if(clickContext.isRightClick()){
                    plugin.getLogger().info( Bukkit.getPlayer(playerID).getName() +" Rightclicked Listing.");
                }
            });
        }
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

    //TODO: add support for all placeholders from config.yml

    private ItemStack getDisplayItem(Listing l){
        ItemStack result = l.content().clone();
        ItemMeta meta = result.getItemMeta();
        String itemName = PlainTextComponentSerializer.plainText().serialize(l.content().displayName());
        String displayName = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getListingDisplayConfiguration().getNameTemplate();
        meta.customName(MiniMessage.miniMessage().deserialize(displayName.replace("{PRICE}", NumberFormat.formatGerman(l.price())).replace("{ITEM_NAME}", itemName)).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getListingDisplayConfiguration().getLoreHeaderTemplates()) {
            String resolvedLine = line
                    .replace("{PRICE}", NumberFormat.formatGerman(l.price()))
                    .replace("{ITEM_NAME}", itemName);
            lore.add(MiniMessage.miniMessage().deserialize(resolvedLine).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        result.setItemMeta(meta);
        return result;
    }

    private void createNavbar(){
        int fromX = layoutState.getAdvancedCategories()== MainPageLayoutState.ButtonLayout.SIDEBAR ? 1 : 0;
        int y = 5;
        int toX = layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.SIDEBAR ? 7 : 8;
        int highestSlot = toX - fromX;

        MainPageGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();

        bottom = inventory.createRegionFromCoords("navbar", fromX, y, toX, y);

        bottom.setItem(
                0,
                configuration.getNavigationArrowLeft(),
                action ->{
                    if(!action.isDoubleClick()) return;
                    center.previousPageAndRefresh();
                }
        );

        int index = 1;

        if(layoutState.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.BUTTON){
            createCategoryItem(index);
            index++;
        }
        else if(layoutState.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.DISABLED){
            index++;
        }

        if(layoutState.getButtonLayout().isShowSettings()){
            bottom.setItem(
                    index,
                    configuration.getSettingsIcon(),
                    action ->{
                        Bukkit.getPlayer(playerID).sendMessage("Clicked Settings icon");
                        pageController.openSettingsPage(playerID);
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowMyListings()){
            bottom.setItem(
                    index,
                    configuration.getMyListingsIcon(),
                    action ->{
                        Bukkit.getPlayer(playerID).sendMessage("Clicked My Listings icon");
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowRefresh()){
            bottom.setItem(
                    index,
                    configuration.getRefreshIcon(),
                    action ->{
                        Bukkit.getPlayer(playerID).sendMessage("Clicked Refresh icon");
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowSort()){
            bottom.setItem(
                    index,
                    configuration.getSortIcon(),
                    action ->{
                        Bukkit.getPlayer(playerID).sendMessage("Clicked Sort icon");
                    }
            );
        }
        index++;


        if(layoutState.getButtonLayout().isShowSearch()){
            bottom.setItem(
                    index,
                    configuration.getSearchIcon(),
                    action ->{
                        Bukkit.getPlayer(playerID).sendMessage("Clicked Search icon");
                    }
            );
        }
        index++;

        if(layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.DISABLED){
        }
        else if(layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.BUTTON){
            bottom.setItem(
                    index,
                    createHistoryItem(null),
                    action ->{
                        Bukkit.getPlayer(playerID).sendMessage("Clicked History icon");
                    }
            );
        }

        bottom.setItem(
                highestSlot,
                configuration.getNavigationArrowRight(),
                action ->{
                    if(!action.isDoubleClick()) return;
                    center.nextPageAndRefresh();
                }
        );
    }

    private void createCategoryItem(int index) {
        bottom.setItem(
                index,
                createCategoryItem(plugin.getAuctionHouse().getPreferencesBlocking(playerID).categoryEntries(), categoryItemSelectedIndex),
                action ->{
                    PlayerPreferences preferences = plugin.getAuctionHouse().getPreferencesBlocking(playerID);
                    if(action.isLeftClick()){
                        if(preferences.categoryEntries().size()>categoryItemSelectedIndex+1){
                            categoryItemSelectedIndex = (categoryItemSelectedIndex+1);
                            filter = (preferences.categoryEntries().get(categoryItemSelectedIndex).filter());
                            createCategoryItem(index);
                            buildCenterContent();
                            center.refresh();
                            bottom.refresh();
                        }
                    }
                    else if(action.isRightClick()){
                        if(categoryItemSelectedIndex> 0){
                            categoryItemSelectedIndex = (categoryItemSelectedIndex-1);
                            filter = (preferences.categoryEntries().get(categoryItemSelectedIndex).filter());
                            createCategoryItem(index);
                            buildCenterContent();
                            center.refresh();
                            bottom.refresh();
                        }
                    }
                }
        );
    }

    private ItemStack createCategoryItem(List<PlayerPreferencesCategoryEntry> categories, int index){
        Bukkit.getLogger().info("inder: "+index);
        List<Component> lore = new ArrayList<>(categories.size());
        for(int i = 0; i < categories.size(); i++){
            if(i==index) {
                lore.add(i, categories.get(i).preview().getItemMeta().displayName().color(TextColor.color(32, 32, 254)));
            }
            else {
                lore.add(i, categories.get(i).preview().getItemMeta().displayName());
            }
        }

        lore.add(MiniMessage.miniMessage().deserialize("<red> Test"));

        ItemStack result = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getCategoriesIcon();
        result.lore(lore);
        return result;
    }

    private ItemStack createHistoryItem(List<AHTransactionHistoryEntry> historyEntries){

        //TODO: implement

        return new ItemStack(Material.CLOCK);
    }
}
