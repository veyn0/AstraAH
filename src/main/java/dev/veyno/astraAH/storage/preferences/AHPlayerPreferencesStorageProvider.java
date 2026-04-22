package dev.veyno.astraAH.storage.preferences;

import dev.veyno.astraAH.entity.PlayerPreferences;
import dev.veyno.astraAH.entity.PlayerPreferencesCategoryEntry;

import java.util.UUID;

@Deprecated(forRemoval = true)
public interface AHPlayerPreferencesStorageProvider {

    PlayerPreferences getPreferences(UUID playerId);

    void setPreferences(UUID playerId, PlayerPreferences preferences);

    void addCategory(UUID playerId, PlayerPreferencesCategoryEntry categoryEntry);

}
