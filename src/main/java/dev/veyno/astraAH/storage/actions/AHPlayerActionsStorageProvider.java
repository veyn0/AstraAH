package dev.veyno.astraAH.storage.actions;

import dev.veyno.astraAH.dto.AllowedPlayerActions;

import java.util.UUID;

@Deprecated(forRemoval = true)
public interface AHPlayerActionsStorageProvider {

    AllowedPlayerActions getAllowedActions(UUID playerId);

    void saveAllowedActions(AllowedPlayerActions actions);

}
