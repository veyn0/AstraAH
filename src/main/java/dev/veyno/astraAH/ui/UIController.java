package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.entity.Listing;
import dev.veyno.astraAH.entity.ListingsFilter;
import dev.veyno.astraAH.ui.error.UIState;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.ItemStackParser;
import dev.veyno.astraAH.util.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UIController {

    private Map<UUID, UIState> playerUiStates = new HashMap<>();

    private final AstraAH plugin;

    private final Component AH_LISTINGS_TITLE;

    private final String LISTING_DISPLAY_NAME_UNRESOLVED;

    private final Component LISTING_DETAILS_TITLE;

    private final List<Component> LISTING_LORE_HEADER;

    private final List<ListingsFilter> filters;

    public UIController(AstraAH plugin) {
        this.plugin = plugin;
        FileConfiguration configuration = plugin.getConfig();
        this.AH_LISTINGS_TITLE = MiniMessage.miniMessage().deserialize(configuration.getString("messages.listings.title", "<red>Error 404. Content Not Found")).decoration(TextDecoration.ITALIC, false);
        this.LISTING_DISPLAY_NAME_UNRESOLVED = configuration.getString("messages.listings.item.display_name", "<red>Error 404. Content Not Found");
        List<Component> loreHeaders = new ArrayList<>();
        for(String s : configuration.getStringList("messages.listings.item.lore_header")){
            loreHeaders.add(MiniMessage.miniMessage().deserialize(s).decoration(TextDecoration.ITALIC, false));
        }
        LISTING_LORE_HEADER = loreHeaders;
        LISTING_DETAILS_TITLE = MiniMessage.miniMessage().deserialize(configuration.getString("messages.listing_info.title", "<red>Error 404. Content Not Found")).decoration(TextDecoration.ITALIC, false);
        filters = getFilter();
    }

    public void onOpenAHUI(Player p){
//        if(playerUiStates.get(p.getUniqueId())==null||!(playerUiStates.get(p.getUniqueId())==UIState.CLOSED||playerUiStates.get(p.getUniqueId())==UIState.CLOSED)){
//            plugin.getErrorHandler().onIllegalInventoryView(p);
//            return;
//        }

        openMainPage(p, null);

    }

    private void openMainPage(Player p, List<Material> filter){
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), AH_LISTINGS_TITLE, p);
        List<Listing> listings = plugin.getAuctionHouse().getListings();
        //List<Listing> listings = createExampleListings();
        //Center: available listings
        ClickableInventory.InventoryRegion centerContent = inventory.createRegion("center", new ClickableInventory.LayoutCenter());
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

        //Left side: categories

        ClickableInventory.InventoryRegion leftContent = inventory.createRegion("left", new ClickableInventory.LayoutSide(true));
        leftContent.setStaticItem(
                0,
                ItemStackParser.parseSection(plugin.getConfig().getConfigurationSection("items.buttons.prev_category"), plugin), action ->{
                leftContent.scrollByAndRefresh(-1);
                });
        leftContent.setStaticItem(
                5,
                ItemStackParser.parseSection(plugin.getConfig().getConfigurationSection("items.buttons.next_category"), plugin), action -> {
                    leftContent.scrollByAndRefresh(1);
                });

        for(ListingsFilter f : filters){
            leftContent.addItem(
                    f.preview(),
                    action ->{
                        openMainPage(action.getPlayer(), f.materials());
                    });
        }

        //Right side: last bought items



        //Bottom: navigation + insert + filter
        ClickableInventory.InventoryRegion bottomContent = inventory.createRegion("bottom", new ClickableInventory.LayoutHorizontalNoSides(6));
        bottomContent.setItem(
                0,
                ItemStackParser.parseSection(plugin.getConfig().getConfigurationSection("items.buttons.prev_page"), plugin),
                action ->{
                    centerContent.previousPageAndRefresh();
                }
        );
        bottomContent.setItem(
                6,
                ItemStackParser.parseSection(plugin.getConfig().getConfigurationSection("items.buttons.next_page"), plugin),
                action ->{
                    centerContent.nextPageAndRefresh();
                }
        );



        inventory.open();

    }



    private void openListingInfo(Player p, Listing l){
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), LISTING_DETAILS_TITLE, p );


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
        meta.customName(MiniMessage.miniMessage().deserialize(LISTING_DISPLAY_NAME_UNRESOLVED.replace("{PRICE}", NumberFormat.formatGerman(l.price())).replace("{ITEM_NAME}", itemName)).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.addAll(LISTING_LORE_HEADER);
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



    private List<ListingsFilter> getFilter(){
        List<ListingsFilter> result = new ArrayList<>();
        FileConfiguration config = plugin.getConfig();
        for(String s : config.getConfigurationSection("categories").getKeys(false)){
            String path = "categories." + s;
            ItemStack item = ItemStackParser.parseSection(config.getConfigurationSection(path+".item"), plugin);
            List<Material> materials = parseMaterialPatterns(config.getStringList(path+".rules"));
            result.add(new ListingsFilter(materials, item));
        }
        return result;
    }


    public static List<Material> parseMaterialPatterns(List<String> patterns) {
        List<Material> result = new ArrayList<>();

        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) continue;

            String upper = pattern.toUpperCase();

            boolean startWild = upper.startsWith("*");
            boolean endWild = upper.endsWith("*");
            String core = upper
                    .substring(startWild ? 1 : 0, endWild ? upper.length() - 1 : upper.length());

            if (startWild && endWild) {
                for (Material m : Material.values())
                    if (m.name().contains(core)) result.add(m);

            } else if (startWild) {
                for (Material m : Material.values())
                    if (m.name().endsWith(core)) result.add(m);

            } else if (endWild) {
                for (Material m : Material.values())
                    if (m.name().startsWith(core)) result.add(m);

            } else {
                Material exact = Material.matchMaterial(upper);
                if (exact != null) result.add(exact);
            }
        }

        return result;
    }
}
