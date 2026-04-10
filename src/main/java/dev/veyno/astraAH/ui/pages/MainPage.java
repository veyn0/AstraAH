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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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

        PlayerPreferences preferences = plugin.getPlayerPreferencesStorageProvider().getPreferences(p.getUniqueId());

        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), mainPageGuiConfiguration.getTitle(), p);

        ClickableInventory.InventoryRegion centerContent = buildCenterContent(p, state, inventory);

        ClickableInventory.InventoryRegion navbar = createNavbar(p, inventory, state, centerContent, preferences);

        if(state.getAdvancedHistory()== MainPageLayoutState.ButtonLayout.SIDEBAR){
            buildCategorySidebar(p, state, inventory, mainPageGuiConfiguration, preferences);
        }

        if(state.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.SIDEBAR){

        }

        inventory.open();
    }

    @Override
    public Component getPageTitle() {
        return plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getTitle();
    }

    private ClickableInventory.InventoryRegion buildCategorySidebar(Player p, MainPageLayoutState layoutState, ClickableInventory inventory, MainPageGuiConfiguration configuration, PlayerPreferences preferences){
        ClickableInventory.InventoryRegion result = inventory.createRegionFromCoords("categories", 0, 0, 0,5);

        result.setStaticItem(
                0,
                new ItemStack(Material.SPECTRAL_ARROW),
                action ->{
                    if(!action.isLeftClick()) return;
                    layoutState.setCategoryScrollIndex(layoutState.getCategoryScrollIndex()-1);
                    result.scrollByAndRefresh(-1);
                }
        );

        result.setStaticItem(
                5,
                new ItemStack(Material.SPECTRAL_ARROW),
                action ->{
                    if(!action.isLeftClick()) return;
                    layoutState.setCategoryScrollIndex(layoutState.getCategoryScrollIndex()+1);

                    if(result.getItemCount() <= layoutState.getCategoryScrollIndex()){
                        layoutState.setCategoryScrollIndex(result.getItemCount()-1);
                    }
                    else {
                        result.scrollByAndRefresh(1);
                    }
                }
        );

        for(PlayerPreferencesCategoryEntry entry : preferences.categoryEntries()){
            result.addItem(
                    entry.preview(),
                    action ->{
                        if(!action.isLeftClick()) return;
                        layoutState.setFilter(entry.filter());
                        open(p, layoutState, null);
                    }
            );
        }
        result.scrollBy(layoutState.getCategoryScrollIndex());
        return result;
    }

    private ClickableInventory.InventoryRegion buildCenterContent(Player p, MainPageLayoutState layoutState, ClickableInventory inventory){
        int fromX = layoutState.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.SIDEBAR ? 1 : 0;
        int fromY = 0;
        int toX = layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.SIDEBAR ? 7 : 8;
        int toY = 4;

        int page = layoutState.getListingsPageIndex();

        List<Listing> listings = sortListings(plugin.getAuctionHouse().getListings(), layoutState.getSortType());

        List<Material> filter = layoutState.getFilter();

        ClickableInventory.InventoryRegion centerContent = inventory.createRegionFromCoords("center", fromX, fromY, toX, toY);
        for(Listing l : listings){
            if(filter != null && !filter.contains(l.content().getType())) continue;
            centerContent.addItem(getDisplayItem(l), clickContext -> {
                if(clickContext.isLeftClick()){
                    plugin.getLogger().info( p.getName()+" Leftclicked Listing.");
                }
                else if(clickContext.isRightClick()){
                    plugin.getLogger().info( p.getName()+" Rightclicked Listing.");
                }
            });
        }

        return centerContent.openPage(page);
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

    private ClickableInventory.InventoryRegion createNavbar(Player p, ClickableInventory inventory, MainPageLayoutState layoutState, ClickableInventory.InventoryRegion centerContent, PlayerPreferences preferences){
        int fromX = layoutState.getAdvancedCategories()== MainPageLayoutState.ButtonLayout.SIDEBAR ? 1 : 0;
        int y = 5;
        int toX = layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.SIDEBAR ? 7 : 8;
        int highestSlot = toX - fromX;

        MainPageGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();

        ClickableInventory.InventoryRegion result = inventory.createRegionFromCoords("navbar", fromX, y, toX, y);

        result.setItem(
                0,
                configuration.getNavigationArrowLeft(),
                action ->{
                    centerContent.previousPageAndRefresh();
                }
        );

        int index = 1;

        if(layoutState.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.BUTTON){
            result.setItem(
                    index,
                    createCategoryItem(preferences.categoryEntries(), layoutState.getCategoryScrollIndex()),
                    action ->{
                        p.sendMessage("Clicked Categories icon");
                        if(action.isLeftClick()){

                        }
                    }
            );

            index++;
        }
        else if(layoutState.getAdvancedCategories() == MainPageLayoutState.ButtonLayout.DISABLED){
            index++;
        }

        if(layoutState.getButtonLayout().isShowSettings()){
            result.setItem(
                    index,
                    configuration.getSettingsIcon(),
                    action ->{
                        p.sendMessage("Clicked Settings icon");
                        pageController.openSettingsPage(p, this);
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowMyListings()){
            result.setItem(
                    index,
                    configuration.getMyListingsIcon(),
                    action ->{
                        p.sendMessage("Clicked My Listings icon");
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowRefresh()){
            result.setItem(
                    index,
                    configuration.getRefreshIcon(),
                    action ->{
                        p.sendMessage("Clicked Refresh icon");
                    }
            );
        }
        index++;

        if(layoutState.getButtonLayout().isShowSort()){
            result.setItem(
                    index,
                    configuration.getSortIcon(),
                    action ->{
                        p.sendMessage("Clicked Sort icon");
                    }
            );
        }
        index++;


        if(layoutState.getButtonLayout().isShowSearch()){
            result.setItem(
                    index,
                    configuration.getSearchIcon(),
                    action ->{
                        p.sendMessage("Clicked Search icon");
                    }
            );
        }
        index++;

        if(layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.DISABLED){
        }
        else if(layoutState.getAdvancedHistory() == MainPageLayoutState.ButtonLayout.BUTTON){
            result.setItem(
                    index,
                    createHistoryItem(null),
                    action ->{
                        p.sendMessage("Clicked History icon");
                    }
            );
        }

        result.setItem(
                highestSlot,
                configuration.getNavigationArrowRight(),
                action ->{
                    centerContent.nextPageAndRefresh();
                }
        );

        return result;
    }

    private ItemStack createCategoryItem(List<PlayerPreferencesCategoryEntry> categories, int index){
        List<Component> lore = new ArrayList<>(categories.size());
        for(int i = 0; i < categories.size(); i++){
            if(i==index) {
                lore.add(i, categories.get(i).preview().displayName().color(TextColor.color(32, 32, 254)));
            }
            else {
                lore.add(i, categories.get(i).preview().displayName());
            }
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
