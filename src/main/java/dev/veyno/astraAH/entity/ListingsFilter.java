package dev.veyno.astraAH.entity;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Deprecated(forRemoval = true)
public record ListingsFilter(
        List<Material> materials,
        ItemStack preview
) {
}
