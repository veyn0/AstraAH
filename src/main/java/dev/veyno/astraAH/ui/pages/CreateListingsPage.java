package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.config.guis.CreateListingGuiConfiguration1;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.IDLocks;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    private final AstraAH plugin;
    private final PageController pageController;
    private final UUID playerId;

    private ClickableInventory inventoryItemSelect;
    private ClickableInventory.InventoryRegion itemSelectContent;
    private ClickableInventory.InventoryRegion itemSelectNavigation;

    private int step = 0;
    private ItemStack selectedItem;
    private double price;

    public CreateListingsPage(AstraAH plugin, PageController pageController, UUID playerId) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerId = playerId;
        buildOnce();
    }

    @Override
    public void buildOnce() {
        CreateListingGuiConfiguration1 configuration = plugin.getConfiguration().getConfiguredGuis().getCreateListingGuiConfiguration1();
        inventoryItemSelect = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerId));
        itemSelectNavigation = inventoryItemSelect.createRegionFromCoords("navigation", 0, 4, 8, 5);
        itemSelectContent = inventoryItemSelect.createRegionFromCoords("content", 0, 0, 8, 3);
        renderNavigation();
        renderContent();
    }

    @Override
    public void show() {
        if (step == 0) {
            inventoryItemSelect.open();
        }
    }

    @Override
    public void reload() {
        step = 0;
        selectedItem = null;
        price = 0;
        renderNavigation();
        renderContent();
        itemSelectNavigation.refresh();
        itemSelectContent.refresh();
    }

    @Override
    public void invalidate(Section section) {
        switch (section) {
            case CONTENT -> { renderContent();    itemSelectContent.refresh(); }
            case NAVBAR  -> { renderNavigation(); itemSelectNavigation.refresh(); }
            case ALL     -> reload();
            default      -> { }
        }
    }

    @Override
    public Component getPageTitle() {
        return inventoryItemSelect.getTitle();
    }

    private void renderNavigation() {
        CreateListingGuiConfiguration1 configuration = plugin.getConfiguration().getConfiguredGuis().getCreateListingGuiConfiguration1();
        itemSelectNavigation.clearItems();

        itemSelectNavigation.setItem(
                9,
                configuration.getCancelIcon(),
                action -> pageController.back()
        );
    }

    private void renderContent() {
        itemSelectContent.clearItems();
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        for (ItemStack i : getAllPlayerItems(player)) {
            itemSelectContent.addItem(
                    i,
                    action -> onItemSelected(i)
            );
        }
    }

    private void onItemSelected(ItemStack item) {
        selectedItem = item;

        itemSelectNavigation.setItem(
                13,
                item,
                () -> { }
        );
        itemSelectNavigation.setItem(
                17,
                new ItemStack(Material.LIME_CONCRETE),
                ctx -> openPriceInput()
        );
        itemSelectNavigation.refresh();
    }

    public void openPriceInput() {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("[Placeholder] set Price"))
                        .inputs(List.of(
                                DialogInput.text("value", Component.text("[Placeholder] Price"))
                                        .initial("00.00")
                                        .maxLength(20)
                                        .width(200)
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("[Placeholder] Done"))
                                .action(DialogAction.customClick(
                                        (view, audience) -> {
                                            if (!(audience instanceof Player p)) return;
                                            if (view == null) return;

                                            String input = view.getText("value");
                                            if (input == null || input.isEmpty()) {
                                                p.sendMessage(Component.text("[Placeholder] Ungültige Eingabe"));
                                                return;
                                            }

                                            try {
                                                double parsed = Double.parseDouble(input);
                                                if (parsed < 0) {
                                                    p.sendMessage(Component.text("[Placeholder] Muss positiv sein"));
                                                    return;
                                                }
                                                price = parsed;
                                                Bukkit.getLogger().info("input: " + parsed);
                                                createListing();
                                            } catch (NumberFormatException e) {
                                                p.sendMessage(Component.text("[Placeholder] Keine Zahl"));
                                            }
                                        },
                                        ClickCallback.Options.builder().uses(1).build()
                                ))
                                .build(),

                        ActionButton.builder(Component.text("[Placeholder] Cancel"))
                                .action(null)
                                .build()
                ))
        );

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) player.showDialog(dialog);
    }

    //TODO: Move this logic to Service Class
    private void createListing() {
        synchronized (IDLocks.getLock(playerId)) {
            if (selectedItem == null || price <= 0) return;
            Player p = Bukkit.getPlayer(playerId);
            if (p == null) return;
            // TODO: createdAt, currency and status should be set by a service/factory once available.
            //       Status 0 represents the active status used by YamlListingsRepository.
            Listing l = new Listing(
                    UUID.randomUUID(),
                    p.getUniqueId(),
                    selectedItem,
                    System.currentTimeMillis(),
                    price,
                    null,
                    0
            );
            if (plugin.getListingController().postListing(l)) {
                p.sendMessage(Component.text("[Placeholder]: Listing created"));
            }
            else {
                p.closeDialog();
                p.closeInventory();
                p.sendMessage("[Placeholder] Failed to create Listing");
            }
        }
        pageController.back();
    }

    public static List<ItemStack> getAllPlayerItems(Player player) {
        PlayerInventory inv = player.getInventory();
        List<ItemStack> items = new ArrayList<>();
        Collections.addAll(items, inv.getContents());
        Collections.addAll(items, inv.getArmorContents());
        items.add(inv.getItemInOffHand());
        items.removeIf(item -> item == null || item.getType().isAir());
        return items;
    }
}