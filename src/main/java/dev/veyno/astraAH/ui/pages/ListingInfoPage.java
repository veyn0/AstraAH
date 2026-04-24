package dev.veyno.astraAH.ui.pages;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.config.guis.ListingInfoGuiConfiguration;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.InteractiveDialogGui;
import io.papermc.paper.dialog.Dialog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class ListingInfoPage implements Page {

    private final AstraAH plugin;
    private final PageController pageController;
    private final UUID playerId;

    private Listing currentListing;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion navBar;
    private ClickableInventory.InventoryRegion content;

    public ListingInfoPage(AstraAH plugin, PageController pageController, UUID playerId) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerId = playerId;
        buildOnce();
    }

    public void setListing(Listing listing) {
        this.currentListing = listing;
        if (inventory != null) {
            renderNavBar();
            renderContent();
            navBar.refresh();
            content.refresh();
        }
    }

    public Listing getCurrentListing() {
        return currentListing;
    }

    @Override
    public void buildOnce() {
        ListingInfoGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getListingInfoGuiConfiguration();
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerId));
        navBar = inventory.createRegionFromCoords("navigation", 0, 3, 8, 5);
        content = inventory.createRegionFromCoords("content", 0, 0, 8, 2);
    }

    @Override
    public void show() {
        inventory.open();
    }

    @Override
    public void reload() {
        if (currentListing == null) return;
        renderNavBar();
        renderContent();
        navBar.refresh();
        content.refresh();
    }

    @Override
    public void invalidate(Section section) {
        if (currentListing == null) return;
        switch (section) {
            case CONTENT -> { renderContent(); content.refresh(); }
            case NAVBAR  -> { renderNavBar();  navBar.refresh(); }
            case ALL     -> reload();
            default      -> { }
        }
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    private void renderNavBar() {
        ListingInfoGuiConfiguration configuration = plugin.getConfiguration().getConfiguredGuis().getListingInfoGuiConfiguration();
        navBar.clearItems();

        navBar.setItem(
                18,
                configuration.getBackIcon(),
                action -> pageController.back()
        );

        navBar.setItem(
                10,
                getSellerHeadIcon(),
                () -> { }
        );

        navBar.setItem(
                12,
                currentListing.getContent(),
                () -> { }
        );

        if (currentListing.getSellerId().equals(playerId)) {
            navBar.setItem(
                    14,
                    configuration.getDeleteIcon(),
                    action -> showDeleteDialog(action.getPlayer())
            );
        }
    }

    private void renderContent() {
        content.clearItems();
        for (ItemStack i : getShulkerContents(currentListing.getContent())) {
            content.addItem(i, () -> { });
        }
    }

    private void showDeleteDialog(Player clicker) {
        Dialog dialog = InteractiveDialogGui.create(Component.text("Confirm Deleting"))
                .message(Component.text("Are you sure you want to Delete your Listing? Already paid taxes will NOT be refunded"))
                .confirmation(
                        Component.text("Yes"),
                        Component.text(""),
                        ctx -> {
                            Player p = ctx.player();
                            // TODO: actually remove the listing via ListingController
                            //   plugin.getListingController().removeListingIfPresent(currentListing.getListingId());
                            p.sendMessage("PlaceHolder - deleted");
                            pageController.back();
                        },
                        Component.text("Cancel"),
                        Component.text(""),
                        ctx -> ctx.player().sendMessage(Component.text("placeholder - deleting canceled"))
                )
                .build();

        clicker.showDialog(dialog);
    }

    public ItemStack getSellerHeadIcon() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        PlayerProfile profile = Bukkit.createProfile(currentListing.getSellerId());
        meta.setPlayerProfile(profile);

        var offline = Bukkit.getOfflinePlayer(currentListing.getSellerId());
        String name = offline.getName();

        meta.displayName(Component.text(
                name != null ? name : "Player",
                NamedTextColor.GOLD
        ));

        meta.lore(List.of(
                Component.text("UUID: " + currentListing.getSellerId(), NamedTextColor.GRAY),
                Component.text("Status: : " + (offline.isOnline() ? "Online" : "Offline"), NamedTextColor.GRAY)
        ));

        head.setItemMeta(meta);
        return head;
    }

    public static ItemStack[] getShulkerContents(ItemStack item) {
        Inventory inv = getShulkerInventory(item);
        return inv != null ? inv.getContents() : new ItemStack[0];
    }

    private static Inventory getShulkerInventory(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) return null;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return null;
        return shulker.getInventory();
    }
}