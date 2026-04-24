package dev.veyno.astraAH.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;

public class SettingsDefaultsConfiguration extends Configurable {

    private final boolean reloadOnOpen;
    private final boolean showAdvancedCategories;
    private final boolean showAdvancedHistory;

    public SettingsDefaultsConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.reloadOnOpen = getBoolean("reload_on_open", false);
        this.showAdvancedCategories = getBoolean("show_advanced_categories", true);
        this.showAdvancedHistory = getBoolean("show_advanced_history", true);
    }

    public boolean isReloadOnOpen() {
        return reloadOnOpen;
    }

    public boolean isShowAdvancedCategories() {
        return showAdvancedCategories;
    }

    public boolean isShowAdvancedHistory() {
        return showAdvancedHistory;
    }
}
