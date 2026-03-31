package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class PlayerListingsGuiConfiguration extends Configurable {

    private Component title ;

    private ItemStack backIcon;
    private ItemStack nextPageIcon;
    private ItemStack previousPageIcon;

    public PlayerListingsGuiConfiguration(String path, AstraAH plugin) {
        super(path, plugin);

        this.title = getMessage("title");

        this.backIcon = getItem("back");
        this.nextPageIcon = getItem("next_page");
        this.previousPageIcon = getItem("previous_page");

    }
}
