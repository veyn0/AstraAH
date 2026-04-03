package dev.veyno.astraAH.ah.configuration.config.settings;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.Configurable;

public class SettingsActionsConfiguration extends Configurable {

    private final ManagePreferencesConfiguration managePreferencesConfiguration;
    private final SettingsToggleMode categories;

    public SettingsActionsConfiguration(AstraAH plugin, String path) {
        super(path, plugin);

        this.managePreferencesConfiguration = new ManagePreferencesConfiguration(getPlugin(), resolvePath("manage_preferences"));
        this.categories = SettingsToggleMode.fromConfigValue(getString("categories", "true"));
    }

    public ManagePreferencesConfiguration getManagePreferencesConfiguration() {
        return managePreferencesConfiguration;
    }

    public SettingsToggleMode getCategories() {
        return categories;
    }
}
