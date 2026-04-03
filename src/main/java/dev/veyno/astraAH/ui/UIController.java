package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.SortType;
import dev.veyno.astraAH.ah.configuration.config.guis.ListingInfoGuiConfiguration;
import dev.veyno.astraAH.ah.configuration.config.guis.main.MainPageGuiConfiguration;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.entity.ListingsFilter;
import dev.veyno.astraAH.entity.ui.PageLayoutState;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UIController {

    private Map<UUID, PageLayoutState> playerUIStates = new ConcurrentHashMap<>();

    private Map<UUID, Listing> pendingCreations = new ConcurrentHashMap<>();

    private final AstraAH plugin;

    public UIController(AstraAH plugin) {
        this.plugin = plugin;
    }

    public void onOpenAHUI(Player p){
        openMainPage(p, null, SortType.NAME_A_Z, false, false);
    }

    private void openMainPage(Player p, PageLayoutState uiLayoutState){

        MainPageGuiConfiguration mainPageGuiConfiguration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();

        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), mainPageGuiConfiguration.getTitle(), p);

        ClickableInventory.InventoryRegion centerContent = createListingsSection(p, filter, sortType, inventory, advancedFilter, quickHistory);

        if(advancedFilter) {
            ClickableInventory.InventoryRegion leftContent = createCategorySection(p, inventory, mainPageGuiConfiguration);
        }

        //Right side: last bought items




        ClickableInventory.InventoryRegion bottomContent = createNavbarSection(p, inventory, advancedFilter, quickHistory, centerContent, mainPageGuiConfiguration);


        inventory.open();

    }

    private ClickableInventory.InventoryRegion createCategorySection(Player p, ClickableInventory inventory, MainPageGuiConfiguration mainPageGuiConfiguration) {
        ClickableInventory.InventoryRegion leftContent = inventory.createRegion("left", new ClickableInventory.LayoutSide(true));
        leftContent.setStaticItem(
                0,
                mainPageGuiConfiguration.getNavigationArrowLeft(), action -> {
                    leftContent.scrollByAndRefresh(-1);
                });
        leftContent.setStaticItem(
                5,
                mainPageGuiConfiguration.getNavigationArrowRight(), action -> {
                    leftContent.scrollByAndRefresh(1);
                });

        for(ListingsFilter f : plugin.getConfiguration().getConfiguredCategories().getListingsFilters()){
            leftContent.addItem(
                    f.preview(),
                    action ->{
                        openMainPage(action.getPlayer(), f.materials(), SortType.NAME_A_Z, false, false);
                    });
        }

        return leftContent;
    }

    private ClickableInventory.InventoryRegion createTransactionHistorySection(Player p, ClickableInventory inventory, boolean advancedFilter, boolean quickHistory, ClickableInventory.InventoryRegion centerContent , MainPageGuiConfiguration mainPageGuiConfiguration) {
        ClickableInventory.InventoryRegion rightContent = inventory.createRegionFromCoords("right", 8,0,8,5);

        rightContent.setStaticItem(
                0,
                mainPageGuiConfiguration.getNavigationArrowLeft(), action -> {
                    rightContent.scrollByAndRefresh(-1);
                });

        rightContent.setStaticItem(
                5,
                mainPageGuiConfiguration.getNavigationArrowRight(), action -> {
                    rightContent.scrollByAndRefresh(1);
                });

        return rightContent;
    }

    private ClickableInventory.InventoryRegion createNavbarSection(Player p, ClickableInventory inventory, boolean advancedFilter, boolean quickHistory, ClickableInventory.InventoryRegion centerContent, MainPageGuiConfiguration mainPageGuiConfiguration) {

        ClickableInventory.InventoryRegion bottomContent = inventory.createRegionFromCoords("bottom", advancedFilter ? 1 : 0, 5, quickHistory? 7 : 8, 5);
        bottomContent.setItem(
                0,
                mainPageGuiConfiguration.getNavigationArrowLeft(),
                action ->{
                    centerContent.previousPageAndRefresh();
                }
        );

        int indexArrow = 8;
        if(advancedFilter) indexArrow--;
        if(quickHistory) indexArrow--;
        bottomContent.setItem(
                indexArrow,
                mainPageGuiConfiguration.getNavigationArrowRight(),
                action ->{
                    centerContent.nextPageAndRefresh();
                }
        );

        return bottomContent;
    }



    private ClickableInventory.InventoryRegion createListingsSection(Player p, List<Material> filter, SortType sortType, ClickableInventory inventory, boolean advancedFilter, boolean quickHistory){
        int fromX = advancedFilter ? 1 : 0;
        int fromY = 0;
        int toX = quickHistory ? 7 : 8;
        int toY = 4;

        List<Listing> listings = sortListings(plugin.getAuctionHouse().getListings(), sortType);

        ClickableInventory.InventoryRegion centerContent = inventory.createRegionFromCoords("center", fromX, fromY, toX, toY);
        for(Listing l : listings){
            if(filter != null && !filter.contains(l.content().getType())) continue;
            centerContent.addItem(getDisplayItem(l), clickContext -> {
                if(clickContext.isLeftClick()){
                    attemptPurchase(p, l);
                }
                else if(clickContext.isRightClick()){
                    openListingInfo(p, l);
                }
            });
        }

        return centerContent;
    }

    private void openCreateListingMenu(Player p){
        Component title = MiniMessage.miniMessage().deserialize("<dark_gray>Auktion erstellen");
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(),title,  p);
        inventory.setRows(3);

        inventory.createRegionFromCoords("main",0,0,8,2);




        inventory.open();
    }

    private void openListingInfo(Player p, Listing l){
        ListingInfoGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getListingInfoGuiConfiguration();
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), p );


        //TODO


    }

    private void attemptPurchase(Player buyer, Listing l){
        buyer.sendMessage("...");
        buyer.closeInventory();
    }


    private ItemStack getDisplayItem(Listing l){
        ItemStack result = l.content().clone();
        ItemMeta meta = result.getItemMeta();
        String itemName = PlainTextComponentSerializer.plainText().serialize(l.content().displayName());
        meta.customName(MiniMessage.miniMessage().deserialize(LISTING_DISPLAY_NAME_TEMPLATE.replace("{PRICE}", NumberFormat.formatGerman(l.price())).replace("{ITEM_NAME}", itemName)).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        for (String line : LISTING_LORE_HEADER_TEMPLATES) {
            String resolvedLine = line
                    .replace("{PRICE}", NumberFormat.formatGerman(l.price()))
                    .replace("{ITEM_NAME}", itemName);
            lore.add(MiniMessage.miniMessage().deserialize(resolvedLine).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        result.setItemMeta(meta);
        return result;
    }





    private static final Material[] SAMPLE_MATERIALS = {
            Material.DIAMOND,
            Material.GOLD_INGOT,
            Material.IRON_INGOT,
            Material.EMERALD,
            Material.NETHERITE_INGOT,
            Material.OAK_LOG,
            Material.COBBLESTONE,
            Material.REDSTONE,
            Material.LAPIS_LAZULI,
            Material.ENDER_PEARL
    };

    public static List<Listing> createExampleListings() {
        List<Listing> listings = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 40; i++) {
            UUID listingId = UUID.randomUUID();
            UUID playerId = UUID.randomUUID();

            Material material = SAMPLE_MATERIALS[random.nextInt(SAMPLE_MATERIALS.length)];
            int amount = 1 + random.nextInt(64);

            ItemStack item = new ItemStack(material, amount);

            double price = Math.round((1 + random.nextDouble() * 1000) * 100.0) / 100.0;

            listings.add(new Listing(
                    listingId,
                    playerId,
                    price,
                    item
            ));
        }

        return listings;
    }

    public List<Listing> sortListings(List<Listing> listings, SortType sortType) {
        if(sortType==null) sortType = SortType.NAME_A_Z;
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
