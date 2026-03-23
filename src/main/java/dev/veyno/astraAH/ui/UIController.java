package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.Listing;
import dev.veyno.astraAH.ui.error.UIState;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
        this.AH_LISTINGS_TITLE = MiniMessage.miniMessage().deserialize(configuration.getString("messages.listings.title", "<red>Error 404. Content Not Found"));
        this.LISTING_DISPLAY_NAME_UNRESOLVED = configuration.getString("messages.listings.item.display_name", "<red>Error 404. Content Not Found");
        List<Component> loreHeaders = new ArrayList<>();
        for(String s : configuration.getStringList("messages.listings.item.lore_header")){
            loreHeaders.add(MiniMessage.miniMessage().deserialize(s));
        }
        LISTING_LORE_HEADER = loreHeaders;
        LISTING_DETAILS_TITLE = MiniMessage.miniMessage().deserialize(configuration.getString("messages.listing_info.title", "<red>Error 404. Content Not Found"));
    }

    public void onOpenAHUI(Player p){
        if(playerUiStates.get(p.getUniqueId())==null||!(playerUiStates.get(p.getUniqueId())==UIState.CLOSED)){
            plugin.getErrorHandler().onIllegalInventoryView(p);
            return;
        }

        openMainPage(p);

    }



    private void openMainPage(Player p){
        ClickableInventory inventory = new ClickableInventory(plugin.getInventoryManager(), AH_LISTINGS_TITLE, p);
        List<Listing> listings = plugin.getAuctionHouse().getListings();
        for(Listing l : listings){
            inventory.addItem(getDisplayItem(l), clickContext -> {
                if(clickContext.isLeftClick()){
                    attemptPurchase(p, l);
                }
                else if(clickContext.isRightClick()){
                    openListingInfo(p, l);
                }
            });
        }



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
        meta.customName(MiniMessage.miniMessage().deserialize(LISTING_DISPLAY_NAME_UNRESOLVED.replace("{PRICE}", NumberFormat.formatGerman(l.price()))));
        List<Component> lore = meta.lore();
        lore.addAll(0, LISTING_LORE_HEADER);
        result.setItemMeta(meta);
        return result;
    }






    public void clearPlayerUIState(UUID pId){
        playerUiStates.put(pId, UIState.CLOSED);
    }
}
