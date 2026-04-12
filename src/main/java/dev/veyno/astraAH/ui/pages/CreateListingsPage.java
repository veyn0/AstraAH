package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.guis.CreateListingGuiConfiguration1;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CreateListingsPage implements Page {

    private AstraAH plugin;
    private UUID playerID;
    private PageController pageController;

    private ClickableInventory inventoryItemSelect;
    private ClickableInventory inventoryConfirm;

    private ClickableInventory.InventoryRegion itemSelectContent;
    private ClickableInventory.InventoryRegion itemSelectNavigation;

    int step = 0;

    private ItemStack selectedItem;
    private String priceText = "";
    private String descriptionText = "";

    public CreateListingsPage(AstraAH plugin, UUID playerID, PageController pageController) {
        this.plugin = plugin;
        this.playerID = playerID;
        this.pageController = pageController;
    }

    @Override
    public void open(Page previousPage) {
        if(step==0) {
            if(inventoryItemSelect==null) rebuild();
            rebuildContent();
            inventoryItemSelect.open();
        }
        if(step==1){

        }
    }


    @Override
    public Component getPageTitle() {
        return null;
    }

    @Override
    public void rebuild() {
        CreateListingGuiConfiguration1 configuration = plugin.getConfiguration().getConfiguredGuis().getCreateListingGuiConfiguration1();
        inventoryItemSelect = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(),Bukkit.getPlayer(playerID));
        itemSelectNavigation = inventoryItemSelect.createRegionFromCoords("navigation", 0,4,8,5);

        itemSelectNavigation.setItem(
                9,
                configuration.getCancelIcon(),
                action ->{
                    pageController.getMyListingsPage(playerID).open(null);
                }
                );

        itemSelectContent = inventoryItemSelect.createRegionFromCoords("content", 0,0,8,3);



    }

    private void rebuildContent(){
        itemSelectContent.clearItems();
        for(ItemStack i : getAllPlayerItems(Bukkit.getPlayer(playerID))){
            itemSelectContent.addItem(
                    i,
                    action ->{
                        selectedItem = i;
                        itemSelectNavigation.setItem(
                                13,
                                i,
                                () ->{}
                        );
                        itemSelectNavigation.setItem(
                                17,
                                new ItemStack(Material.LIME_CONCRETE),
                                ctx ->{
                                    openPriceInput();
                                }
                        );
                        itemSelectNavigation.refresh();
                    }
            );
        }
    }

    public static List<ItemStack> getAllPlayerItems(Player player) {
        PlayerInventory inv = player.getInventory();
        List<ItemStack> items = new ArrayList<>();

        // Main + Hotbar
        Collections.addAll(items, inv.getContents());

        // Armor
        Collections.addAll(items, inv.getArmorContents());

        // Offhand
        items.add(inv.getItemInOffHand());
        items.removeIf(item -> item == null || item.getType().isAir());
        return items;
    }

    public void openPriceInput() {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("set Price"))
                        .inputs(List.of(
                                DialogInput.text("value", Component.text("Price"))
                                        .initial("00.00")
                                        .maxLength(20)
                                        .width(200)
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Done"))
                                .action(DialogAction.customClick(
                                        (view, audience) -> {
                                            if (!(audience instanceof Player p)) return;
                                            if (view == null) return;

                                            String input = view.getText("value");
                                            if (input == null || input.isEmpty()) {
                                                p.sendMessage(Component.text("Ungültige Eingabe"));
                                                return;
                                            }

                                            try {
                                                double parsed = Double.parseDouble(input);

                                                if (parsed < 0) {
                                                    p.sendMessage(Component.text("Muss positiv sein"));
                                                    return;
                                                }

                                                Bukkit.getLogger().info("input: " + parsed);



                                            } catch (NumberFormatException e) {
                                                p.sendMessage(Component.text("Keine Zahl"));
                                            }
                                        },
                                        ClickCallback.Options.builder().uses(1).build()
                                ))
                                .build(),

                        ActionButton.builder(Component.text("Abbrechen"))
                                .action(null)
                                .build()
                ))
        );

        Bukkit.getPlayer(playerID).showDialog(dialog);
    }


    @Override
    public void refresh() {

    }
}
