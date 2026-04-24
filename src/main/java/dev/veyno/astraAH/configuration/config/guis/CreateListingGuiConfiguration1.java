package dev.veyno.astraAH.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class CreateListingGuiConfiguration1 extends Configurable {

    private final Component title;

    private final ItemStack cancelIcon;


    public CreateListingGuiConfiguration1(AstraAH plugin, String path) {
        super(path, plugin);

        this.title = getMessage("title");

        this.cancelIcon = getItem("cancel");

    }

    public Component getTitle() {
        return title;
    }

    public ItemStack getCancelIcon() {
        return cancelIcon.clone();
    }
}
