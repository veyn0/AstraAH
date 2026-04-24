package dev.veyno.astraAH.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.configuration.Configurable;

public class SettingsActionsConfiguration extends Configurable {

    private final ManagePreferencesConfiguration managePreferencesConfiguration;
    private final SettingsToggleMode categories;
    private final SettingsToggleMode settings;
    private final SettingsToggleMode myListings;
    private final SettingsToggleMode refresh;
    private final SettingsToggleMode sort;
    private final SettingsToggleMode search;
    private final SettingsToggleMode history;

    public SettingsActionsConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.managePreferencesConfiguration = new ManagePreferencesConfiguration(getPlugin(), resolvePath("manage_preferences"));
        this.categories = SettingsToggleMode.fromConfigValue(getString("categories", "true"));
        this.settings = SettingsToggleMode.fromConfigValue(getString("settings", "true"));
        this.myListings = SettingsToggleMode.fromConfigValue(getString("my_listings", "true"));
        this.refresh = SettingsToggleMode.fromConfigValue(getString("refresh", "true"));
        this.sort = SettingsToggleMode.fromConfigValue(getString("sort", "true"));
        this.search = SettingsToggleMode.fromConfigValue(getString("search", "true"));
        this.history = SettingsToggleMode.fromConfigValue(getString("history", "true"));
    }

    public ManagePreferencesConfiguration getManagePreferencesConfiguration() {
        return managePreferencesConfiguration;
    }

    public SettingsToggleMode getCategories() {
        return categories;
    }

    public SettingsToggleMode getSettings() {
        return settings;
    }

    public SettingsToggleMode getMyListings() {
        return myListings;
    }

    public SettingsToggleMode getRefresh() {
        return refresh;
    }

    public SettingsToggleMode getSort() {
        return sort;
    }

    public SettingsToggleMode getSearch() {
        return search;
    }

    public SettingsToggleMode getHistory() {
        return history;
    }
}
