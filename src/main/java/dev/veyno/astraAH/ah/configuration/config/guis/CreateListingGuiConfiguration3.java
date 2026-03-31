package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class CreateListingGuiConfiguration3 extends Configurable {

    private Component title;

    private ItemStack confirmIcon;
    private ItemStack cancelIcon;

    public CreateListingGuiConfiguration3(String path, AstraAH plugin) {
        super(path, plugin);

        this.title = getMessage("title");

        this.confirmIcon = getItem("confirm");
        this.cancelIcon = getItem("cancel");

    }

}
