package dev.veyno.astraAH.ah.configuration.config.guis;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public class SettingsGuiConfiguration extends Configurable {

    private final Component title;

    private final ItemStack categoryToggleIcon;
    private final ItemStack historyToggleIcon;
    private final ItemStack defaultFilterIcon;
    private final ItemStack defaultCategoryIcon;



    public SettingsGuiConfiguration(String path, AstraAH plugin) {
        super(path, plugin);

        this.title = getMessage("title");

        this.categoryToggleIcon = getItem("categories_toggle");
        this.historyToggleIcon = getItem("history_toggle");
        this.defaultFilterIcon = getItem("default_filter");
        this.defaultCategoryIcon = getItem("default_category");

    }

    public Component getTitle() {
        return title;
    }

    public ItemStack getCategoryToggleIcon() {
        return categoryToggleIcon.clone();
    }

    public ItemStack getHistoryToggleIcon() {
        return historyToggleIcon.clone();
    }

    public ItemStack getDefaultFilterIcon() {
        return defaultFilterIcon.clone();
    }

    public ItemStack getDefaultCategoryIcon() {
        return defaultCategoryIcon.clone();
    }
}
