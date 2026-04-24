package dev.veyno.astraAH.configuration.config.settings;

import java.util.Locale;

public enum SettingsToggleMode {
    ENABLED,
    DISABLED,
    PERMISSION;

    public static SettingsToggleMode fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            return ENABLED;
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true" -> ENABLED;
            case "false" -> DISABLED;
            case "permission" -> PERMISSION;
            default -> throw new IllegalArgumentException("Unsupported settings toggle mode: " + value);
        };
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean requiresPermission() {
        return this == PERMISSION;
    }
}
