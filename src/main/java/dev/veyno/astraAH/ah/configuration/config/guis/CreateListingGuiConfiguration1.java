package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class CreateListingGuiConfiguration1 extends Configurable {

    private Component title;

    private ItemStack cancelIcon;


    public CreateListingGuiConfiguration1(AstraAH plugin, String path) {
        super(path, plugin);

        this.title = getMessage("title");

        this.cancelIcon = getItem("cancel");

    }

}
