package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class ListingInfoGuiConfiguration extends Configurable {

    private final Component title;
    private final boolean showSellerHead;
    private final ItemStack sellerHeadIcon;
    private final ItemStack infoIcon;
    private final ItemStack backIcon;
    private final ItemStack deleteIcon;

    public ListingInfoGuiConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.title = getMessage("title");
        this.showSellerHead = getBoolean("show_seller_head", false);
        this.sellerHeadIcon = getItem("seller_head");
        this.infoIcon = getItem("info");
        this.backIcon = getItem("back");
        this.deleteIcon = getItem("delete");
    }

    public Component getTitle() {
        return title;
    }

    public boolean isShowSellerHead() {
        return showSellerHead;
    }

    public ItemStack getSellerHeadIcon() {
        return sellerHeadIcon.clone();
    }

    public ItemStack getInfoIcon() {
        return infoIcon.clone();
    }

    public ItemStack getBackIcon() {
        return backIcon.clone();
    }

    public ItemStack getDeleteIcon() {
        return deleteIcon;
    }
}
