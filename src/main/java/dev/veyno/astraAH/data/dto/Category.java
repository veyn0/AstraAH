package dev.veyno.astraAH.data.dto;


import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class Category {
    private List<String> filter;
    private ItemStack preview;

    public static List<Category> configuredDefaults(UUID playerId){
        //TODO: use defaults from configuration
        return List.of();
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public ItemStack getPreview() {
        return preview;
    }

    public void setPreview(ItemStack preview) {
        this.preview = preview;
    }
}
