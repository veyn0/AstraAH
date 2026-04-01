package dev.veyno.astraAH.ah.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;

public class DefaultLayoutConfiguration extends Configurable {

    private final boolean categoriesLeft;
    private final boolean historyRight;

    public DefaultLayoutConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.categoriesLeft = getBoolean("categories_left", true);
        this.historyRight = getBoolean("history_right", true);
    }

    public boolean isCategoriesLeft() {
        return categoriesLeft;
    }

    public boolean isHistoryRight() {
        return historyRight;
    }
}
