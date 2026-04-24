package dev.veyno.astraAH.configuration.config.categories;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import dev.veyno.astraAH.util.ItemStackParser;
import dev.veyno.astraAH.util.MaterialPatternParser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CategoryFilterConfiguration extends Configurable {

    private final String id;
    private final ItemStack previewItem;
    private final List<String> rules;
    private final List<Material> materials;

    public CategoryFilterConfiguration(AstraAH plugin, String path, String id) {
        super(path, plugin);

        this.id = id;
        this.previewItem = ItemStackParser.parseSection(getSection("item"), plugin);
        this.rules = List.copyOf(getStringList("rules"));
        this.materials = List.copyOf(MaterialPatternParser.parse(rules));
    }

    public String getId() {
        return id;
    }

    public ItemStack getPreviewItem() {
        return previewItem.clone();
    }

    public List<String> getRules() {
        return rules;
    }

    public List<Material> getMaterials() {
        return materials;
    }

//    public ListingsFilter toListingsFilter() {
//        return new ListingsFilter(materials, getPreviewItem());
//    }
}
