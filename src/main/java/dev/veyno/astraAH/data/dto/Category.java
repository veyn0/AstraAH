package dev.veyno.astraAH.data.dto;


import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.config.categories.CategoryFilterConfiguration;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Category {
    private List<String> filter;
    private ItemStack preview;

    public Category() {
    }

    public Category(List<String> filter, ItemStack preview) {
        this.filter = filter;
        this.preview = preview;
    }

    public static List<Category> configuredDefaults(UUID playerId, AstraAH plugin){
        //TODO: use defaults from configuration

        List<Category> result = new ArrayList<>();
        for(CategoryFilterConfiguration filter : plugin.getConfiguration().getConfiguredCategories().getFilters()){
            result.add(new Category(filter.getRules(), filter.getPreviewItem()));
        }

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
