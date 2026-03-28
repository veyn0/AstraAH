package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.Listing;
import dev.veyno.astraAH.ui.error.UIState;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.ItemStackParser;
import dev.veyno.astraAH.util.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
    }

    public void onOpenAHUI(Player p){
//        if(playerUiStates.get(p.getUniqueId())==null||!(playerUiStates.get(p.getUniqueId())==UIState.CLOSED||playerUiStates.get(p.getUniqueId())==UIState.CLOSED)){
//            plugin.getErrorHandler().onIllegalInventoryView(p);
//            return;
//        }

        openMainPage(p);

    }

    private void openMainPage(Player p){
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), AH_LISTINGS_TITLE, p);
        List<Listing> listings = plugin.getAuctionHouse().getListings();
        //List<Listing> listings = createExampleListings();
        //Center: available listings
        ClickableInventory.InventoryRegion centerContent = inventory.createRegion("center", new ClickableInventory.LayoutCenter());
        for(Listing l : listings){
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

        //Right side: last bought items

        //Bottom: navigation + insert + filter
        ClickableInventory.InventoryRegion bottomContent = inventory.createRegion("bottom", new ClickableInventory.LayoutCenter());
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
                    centerContent.previousPageAndRefresh();
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




    public void clearPlayerUIState(UUID pId){
        playerUiStates.put(pId, UIState.CLOSED);
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


}
