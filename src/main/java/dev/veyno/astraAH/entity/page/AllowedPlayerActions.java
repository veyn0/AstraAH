package dev.veyno.astraAH.entity.page;

import java.util.UUID;

public class AllowedPlayerActions {

    private final UUID playerId;
    private final PreferencesPlayerActions preferencesPlayerActions;
    private final ActionState categories;
    private final ActionState settings;
    private final ActionState myListings;
    private final ActionState refresh;
    private final ActionState sort;
    private final ActionState search;
    private final ActionState history;

    public AllowedPlayerActions(UUID playerId) {
        this(
                playerId,
                new PreferencesPlayerActions(),
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED
        );
    }

    public AllowedPlayerActions(
            UUID playerId,
            PreferencesPlayerActions preferencesPlayerActions,
            ActionState categories,
            ActionState settings,
            ActionState myListings,
            ActionState refresh,
            ActionState sort,
            ActionState search,
            ActionState history
    ) {
        this.playerId = playerId;
        this.preferencesPlayerActions = preferencesPlayerActions == null ? new PreferencesPlayerActions() : preferencesPlayerActions;
        this.categories = normalizeActionState(categories);
        this.settings = normalizeActionState(settings);
        this.myListings = normalizeActionState(myListings);
        this.refresh = normalizeActionState(refresh);
        this.sort = normalizeActionState(sort);
        this.search = normalizeActionState(search);
        this.history = normalizeActionState(history);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PreferencesPlayerActions getPreferencesPlayerActions() {
        return preferencesPlayerActions;
    }

    public ActionState getCategories() {
        return categories;
    }

    public ActionState getSettings() {
        return settings;
    }

    public ActionState getMyListings() {
        return myListings;
    }

    public ActionState getRefresh() {
        return refresh;
    }

    public ActionState getSort() {
        return sort;
    }

    public ActionState getSearch() {
        return search;
    }

    public ActionState getHistory() {
        return history;
    }

    private ActionState normalizeActionState(ActionState actionState) {
        return actionState == null ? ActionState.UNDEFINED : actionState;
    }
}
