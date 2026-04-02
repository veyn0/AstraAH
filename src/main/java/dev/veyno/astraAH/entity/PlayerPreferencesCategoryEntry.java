package dev.veyno.astraAH.entity;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record PlayerPreferencesCategoryEntry(
        ItemStack preview,
        List<Material> filter
) {

    public PlayerPreferencesCategoryEntry {
        filter = filter == null ? List.of() : List.copyOf(filter);
    }
}
