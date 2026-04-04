package dev.veyno.astraAH.ah.configuration.config.guis.main;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;

import java.util.List;

public class MainPageListingDisplayConfiguration extends Configurable {

    private final String nameTemplate;
    private final List<String> loreHeaderTemplates;

    public MainPageListingDisplayConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.nameTemplate = getString("listing.name", "{ITEM_NAME}");
        this.loreHeaderTemplates = List.copyOf(getStringList("listing.lore_header"));
    }

    public String getNameTemplate() {
        return nameTemplate;
    }

    public List<String> getLoreHeaderTemplates() {
        return loreHeaderTemplates;
    }
}
