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
import dev.veyno.astraAH.util.InteractiveDialogGui;
import dev.veyno.astraAH.util.NumberFormat;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
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
import java.util.function.Consumer;

public class MainPage implements Page {

    /*
    TODO: pro spieler instanzieren, verwerfen das nicht persistente dinge in layoutstate gespeichert werden, alles soll auf basis des Clickableinventory objekts passieren und nciht doppelt gespeichert werden.
     */

    private final AstraAH plugin;

    private final PageController pageController;

    private List<Material> filter;

    private SortType sortType;

    private UUID playerID;

    private int categoryItemSelectedIndex = 0;
    private int historySelectedIndex = 0;
    private int sortTypeSelectedIndex = 0;

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

    public void setLayoutState(MainPageLayoutState layoutState) {
        this.layoutState = layoutState;
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
        sortType = layoutState.getSortType();
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerID) );
        createNavbar();
        buildCenterContent();
        if(layoutState.getAdvancedCategories()== MainPageLayoutState.ButtonLayout.SIDEBAR) {
            buildCategorySidebar();
        }
    }

    @Override
    public void refresh() {
        resetFilter();
        categoryItemSelectedIndex = 0;
        historySelectedIndex = 0;
        sortTypeSelectedIndex = 0;
        rebuild();
    }

    public void resetFilter() {
        this.filter = layoutState.getFilter();
    }

    private void buildCategorySidebar(){
        sidebarLeft = inventory.createRegionFromCoords("categories", 0, 0, 0,5);

        sidebarLeft.setStaticItem(
                0,
                configuration.getScrollUpCategoriesIcon(),
                action ->{
                    if(!action.isLeftClick()) return;
                    sidebarLeft.scrollByAndRefresh(-1);
                }
        );

        sidebarLeft.setStaticItem(
                5,
                configuration.getScrollDownCategoriesIcon(),
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

        List<Listing> listings = sortListings(plugin.getAuctionHouse().getListings(), sortType);

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

                        Dialog dialog = InteractiveDialogGui.create(Component.text("Hinweis"))
                                .message(Component.text("Das hier hat keine funktion lol"))
                                .notice(
                                        Component.text("OK"),
                                        Component.text("Dialog schließen"),
                                        () -> {}
                                )
                                .build();
                        action.getPlayer().showDialog(dialog);
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
                        refresh();
                        open(null);
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowSort()){
            createSortItem(index);
        }
        index++;


        if(layoutState.getButtonLayout().isShowSearch()){
            bottom.setItem(
                    index,
                    configuration.getSearchIcon(),
                    action ->{
                        openSearchDialog(action.getPlayer());
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

    private void openSearchDialog(Player p){
        Bukkit.getPlayer(playerID).sendMessage("Clicked Search icon");
        Dialog dialog = InteractiveDialogGui.create(Component.text("Search"))
                .input(
                        DialogInput.text(
                                "value",
                                Component.text("Search term:")
                        ).build()
                )
                .confirmation(
                        Component.text("OK"),
                        null,
                        ctx -> {
                            String value = ctx.getText("value");
                            onSearch(value);
                        },
                        Component.text("Cancel"),
                        null,
                        (Consumer<InteractiveDialogGui.DialogContext>) null
                )
                .build();
        p.showDialog(dialog);
    }

    private void onSearch(String searchTerm){
        searchTerm = searchTerm.replace(" ", "_");
        List<Material> result = new ArrayList<>();
        for(Material m : Material.values()){
            if(m.name().toLowerCase().contains(searchTerm)){
                result.add(m);
            }
        }
        refresh();
        filter = result;
        rebuild();
        open(null);
    }

    private void createSortItem(int index) {
        bottom.setItem(
                index,
                createSortItem(sortType),
                action ->{
                    if(action.isLeftClick()){
                        if(sortTypeSelectedIndex<5){
                            sortTypeSelectedIndex++;
                            sortType = getSortType(sortTypeSelectedIndex);
                            createSortItem(index);
                            buildCenterContent();
                            center.refresh();
                            bottom.refresh();
                        }
                    }
                    else if(action.isRightClick()){
                        if(sortTypeSelectedIndex> 0){
                            sortTypeSelectedIndex--;
                            sortType = getSortType(sortTypeSelectedIndex);
                            createSortItem(index);
                            buildCenterContent();
                            center.refresh();
                            bottom.refresh();
                        }
                    }
                }
        );
    }

    private SortType getSortType(int index){
        for(SortType t : SortType.values()){
            if(t.getIndex()==index) return t;
        }
        return null;
    }

    private ItemStack createSortItem(SortType type){
        MainPageGuiConfiguration guiConfiguration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();
        ItemStack result = guiConfiguration.getSortIcon();

        List<Component> lore = new ArrayList<>();
        lore.add(
                (
                type==SortType.NAME_A_Z ?
                        guiConfiguration.getMainPageSortOptionsConfiguration().getNameAZEnabled()
                        : guiConfiguration.getMainPageSortOptionsConfiguration().getNameAZDisabled()
                ).decoration(TextDecoration.ITALIC,false)
        );
        lore.add(
                (
                type==SortType.NAME_Z_A ?
                        guiConfiguration.getMainPageSortOptionsConfiguration().getNameZAEnabled()
                        : guiConfiguration.getMainPageSortOptionsConfiguration().getNameZADisabled()
                ).decoration(TextDecoration.ITALIC,false)
        );
        lore.add(
                (
                type==SortType.PRICE_H_L ?
                        guiConfiguration.getMainPageSortOptionsConfiguration().getPriceHLEnabled()
                        : guiConfiguration.getMainPageSortOptionsConfiguration().getPriceHLDisabled()
                ).decoration(TextDecoration.ITALIC,false)
        );
        lore.add(
                (
                type==SortType.PRICE_L_H ?
                        guiConfiguration.getMainPageSortOptionsConfiguration().getPriceLHEnabled()
                        : guiConfiguration.getMainPageSortOptionsConfiguration().getPriceLHDisabled()
                ).decoration(TextDecoration.ITALIC,false)
        );
        lore.add(
                (
                        type==SortType.PRICE_PER_PIECE_H_L ?
                                guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceHLEnabled()
                                : guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceHLDisabled()
                ).decoration(TextDecoration.ITALIC,false)
        );
        lore.add(
                (
                type==SortType.PRICE_PER_PICE_L_H ?
                        guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceLHEnabled()
                        : guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceLHDisabled()
                ).decoration(TextDecoration.ITALIC,false)
        );

        for(Component c : guiConfiguration.getSortIcon().lore()){
            lore.add(c);
        }

        result.lore(lore);

        return result;
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
        List<Component> lore = new ArrayList<>(categories.size());
        lore.add(Component.text(" "));
        for(int i = 0; i < categories.size(); i++){
            if(i==index) {
                String name = PlainTextComponentSerializer.plainText().serialize(categories.get(i).preview().getItemMeta().displayName());
                Component displayName = MiniMessage.miniMessage().deserialize(configuration.getCategoryEnabled().replace("{NAME}", name)).decoration(TextDecoration.ITALIC,false);
                lore.add(i, displayName);
            }
            else {
                String name = PlainTextComponentSerializer.plainText().serialize(categories.get(i).preview().getItemMeta().displayName());
                Component displayName = MiniMessage.miniMessage().deserialize(configuration.getCategoryDisabled().replace("{NAME}", name)).decoration(TextDecoration.ITALIC,false);
                lore.add(i, displayName);
            }
        }

        for(Component c : configuration.getCategoriesIcon().lore()){
            lore.add(c.decoration(TextDecoration.ITALIC,false));
        }

        ItemStack result = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getCategoriesIcon();
        result.lore(lore);
        return result;
    }

    private ItemStack createHistoryItem(List<AHTransactionHistoryEntry> historyEntries){

        //TODO: implement

        return new ItemStack(Material.CLOCK);
    }
}
