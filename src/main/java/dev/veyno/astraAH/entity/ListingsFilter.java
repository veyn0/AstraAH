package dev.veyno.astraAH.entity;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record ListingsFilter(
        List<Material> materials,
        ItemStack preview
) {
}
