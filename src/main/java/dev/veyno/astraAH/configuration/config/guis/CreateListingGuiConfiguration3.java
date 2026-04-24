package dev.veyno.astraAH.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class CreateListingGuiConfiguration3 extends Configurable {

    private final Component title;

    private final ItemStack confirmIcon;
    private final ItemStack cancelIcon;

    public CreateListingGuiConfiguration3(String path, AstraAH plugin) {
        super(path, plugin);

        this.title = getMessage("title");

        this.confirmIcon = getItem("confirm");
        this.cancelIcon = getItem("cancel");

    }

    public Component getTitle() {
        return title;
    }

    public ItemStack getConfirmIcon() {
        return confirmIcon.clone();
    }

    public ItemStack getCancelIcon() {
        return cancelIcon.clone();
    }
}
