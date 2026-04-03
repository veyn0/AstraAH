package dev.veyno.astraAH.entity;

public class PreferencesPlayerActions {

    private final ActionState showAdvancedCategories;
    private final ActionState showAdvancedHistory;
    private final ActionState reloadOnOpen;
    private final ActionState defaultFilter;
    private final ActionState defaultSort;

    public PreferencesPlayerActions() {
        this(
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED
        );
    }

    public PreferencesPlayerActions(ActionState showAdvancedCategories, ActionState showAdvancedHistory, ActionState reloadOnOpen, ActionState defaultFilter, ActionState defaultSort) {
        this.showAdvancedCategories = normalizeActionState(showAdvancedCategories);
        this.showAdvancedHistory = normalizeActionState(showAdvancedHistory);
        this.reloadOnOpen = normalizeActionState(reloadOnOpen);
        this.defaultFilter = normalizeActionState(defaultFilter);
        this.defaultSort = normalizeActionState(defaultSort);
    }

    public ActionState getShowAdvancedCategories() {
        return showAdvancedCategories;
    }

    public ActionState getShowAdvancedHistory() {
        return showAdvancedHistory;
    }

    public ActionState getReloadOnOpen() {
        return reloadOnOpen;
    }

    public ActionState getDefaultFilter() {
        return defaultFilter;
    }

    public ActionState getDefaultSort() {
        return defaultSort;
    }

    private ActionState normalizeActionState(ActionState actionState) {
        return actionState == null ? ActionState.UNDEFINED : actionState;
    }
}
