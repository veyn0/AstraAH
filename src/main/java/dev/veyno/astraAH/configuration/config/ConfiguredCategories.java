package dev.veyno.astraAH.configuration.config;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;
import dev.veyno.astraAH.configuration.config.categories.CategoryFilterConfiguration;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ConfiguredCategories extends Configurable {

    private final List<CategoryFilterConfiguration> filters;

    public ConfiguredCategories(AstraAH plugin, String path) {
        super(path, plugin);

        ConfigurationSection section = getSection("");
        List<CategoryFilterConfiguration> loadedFilters = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            loadedFilters.add(new CategoryFilterConfiguration(getPlugin(), resolvePath(key), key));
        }
        this.filters = List.copyOf(loadedFilters);
    }

    public List<CategoryFilterConfiguration> getFilters() {
        return filters;
    }

//    public List<ListingsFilter> getListingsFilters() {
//        return filters.stream()
//                .map(CategoryFilterConfiguration::toListingsFilter)
//                .toList();
//    }
}
