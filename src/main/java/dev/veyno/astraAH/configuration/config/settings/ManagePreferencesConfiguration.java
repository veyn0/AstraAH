package dev.veyno.astraAH.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;

public class ManagePreferencesConfiguration extends Configurable {

    private final SettingsToggleMode showAdvancedCategories;
    private final SettingsToggleMode showAdvancedHistory;
    private final SettingsToggleMode reloadOnOpen;
    private final SettingsToggleMode defaultFilter;
    private final SettingsToggleMode defaultSort;

    public ManagePreferencesConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.showAdvancedCategories = SettingsToggleMode.fromConfigValue(getString("show_advanced_categories", "true"));
        this.showAdvancedHistory = SettingsToggleMode.fromConfigValue(getString("show_advanced_history", "true"));
        this.reloadOnOpen = SettingsToggleMode.fromConfigValue(getString("reload_on_open", "true"));
        this.defaultFilter = SettingsToggleMode.fromConfigValue(getString("default_filter", "true"));
        this.defaultSort = SettingsToggleMode.fromConfigValue(getString("default_sort", "true"));
    }

    public SettingsToggleMode getShowAdvancedCategories() {
        return showAdvancedCategories;
    }

    public SettingsToggleMode getShowAdvancedHistory() {
        return showAdvancedHistory;
    }

    public SettingsToggleMode getReloadOnOpen() {
        return reloadOnOpen;
    }

    public SettingsToggleMode getDefaultFilter() {
        return defaultFilter;
    }

    public SettingsToggleMode getDefaultSort() {
        return defaultSort;
    }
}
