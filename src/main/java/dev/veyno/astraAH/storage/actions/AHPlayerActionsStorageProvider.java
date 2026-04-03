package dev.veyno.astraAH.storage.actions;

import dev.veyno.astraAH.entity.AllowedPlayerActions;

import java.util.UUID;

public interface AHPlayerActionsStorageProvider {

    AllowedPlayerActions getAllowedActions(UUID playerId);

    void saveAllowedActions(AllowedPlayerActions actions);

}
