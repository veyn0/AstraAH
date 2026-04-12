package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class PlayerListingsGuiConfiguration extends Configurable {

    private final Component title ;

    private final ItemStack backIcon;
    private final ItemStack nextPageIcon;
    private final ItemStack previousPageIcon;
    private final ItemStack createListingIcon;

    public PlayerListingsGuiConfiguration(String path, AstraAH plugin) {
        super(path, plugin);

        this.title = getMessage("title");

        this.backIcon = getItem("back");
        this.nextPageIcon = getItem("next_page");
        this.previousPageIcon = getItem("previous_page");
        this.createListingIcon = getItem("create_listing");
    }

    public Component getTitle() {
        return title;
    }

    public ItemStack getBackIcon() {
        return backIcon.clone();
    }

    public ItemStack getNextPageIcon() {
        return nextPageIcon.clone();
    }

    public ItemStack getPreviousPageIcon() {
        return previousPageIcon.clone();
    }

    public ItemStack getCreateListingIcon() {
        return createListingIcon;
    }
}
