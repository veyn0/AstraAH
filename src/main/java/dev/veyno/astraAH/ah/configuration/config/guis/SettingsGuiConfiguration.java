package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class SettingsGuiConfiguration extends Configurable {

    private Component title;

    private ItemStack categoryToggleIcon;
    private ItemStack historyToggleIcon;
    private ItemStack defaultFilterIcon;
    private ItemStack defaultCategoryIcon;



    public SettingsGuiConfiguration(String path, AstraAH plugin) {
        super(path, plugin);

        this.title = getMessage("title");

        this.categoryToggleIcon = getItem("categories_toggle");
        this.historyToggleIcon = getItem("history_toggle");
        this.defaultFilterIcon = getItem("default_filter");
        this.defaultCategoryIcon = getItem("default_category");

    }
}
