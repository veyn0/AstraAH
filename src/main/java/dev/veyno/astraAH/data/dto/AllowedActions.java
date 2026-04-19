package dev.veyno.astraAH.data.dto;

import dev.veyno.astraAH.dto.ActionState;

import java.util.UUID;

public class AllowedActions {
    private ActionState categories;
    private ActionState settings;
    private ActionState myListings;
    private ActionState refresh;
    private ActionState sort;
    private ActionState search;
    private ActionState history;
    private ActionState showAdvancedCategories;
    private ActionState showAdvancedHistory;
    private ActionState reloadOnOpen;
    private ActionState defaultFilter;
    private ActionState defaultSort;


    public static AllowedActions configuredDefaults(UUID playerId){
        //TODO: make wok
        AllowedActions allowedActions = new AllowedActions();
        allowedActions.setCategories(ActionState.UNDEFINED);
        allowedActions.setSettings(ActionState.UNDEFINED);
        allowedActions.setMyListings(ActionState.UNDEFINED);
        allowedActions.setRefresh(ActionState.UNDEFINED);
        allowedActions.setSort(ActionState.UNDEFINED);
        allowedActions.setSearch(ActionState.UNDEFINED);
        allowedActions.setHistory(ActionState.UNDEFINED);
        allowedActions.setShowAdvancedCategories(ActionState.UNDEFINED);
        allowedActions.setShowAdvancedHistory(ActionState.UNDEFINED);
        allowedActions.setReloadOnOpen(ActionState.UNDEFINED);
        allowedActions.setDefaultFilter(ActionState.UNDEFINED);
        allowedActions.setDefaultSort(ActionState.UNDEFINED);
        return allowedActions;
    }

    public void setCategories(ActionState categories) {
        this.categories = categories;
    }

    public void setSettings(ActionState settings) {
        this.settings = settings;
    }

    public void setMyListings(ActionState myListings) {
        this.myListings = myListings;
    }

    public void setRefresh(ActionState refresh) {
        this.refresh = refresh;
    }

    public void setSort(ActionState sort) {
        this.sort = sort;
    }

    public void setSearch(ActionState search) {
        this.search = search;
    }

    public void setHistory(ActionState history) {
        this.history = history;
    }

    public void setShowAdvancedCategories(ActionState showAdvancedCategories) {
        this.showAdvancedCategories = showAdvancedCategories;
    }

    public void setShowAdvancedHistory(ActionState showAdvancedHistory) {
        this.showAdvancedHistory = showAdvancedHistory;
    }

    public void setReloadOnOpen(ActionState reloadOnOpen) {
        this.reloadOnOpen = reloadOnOpen;
    }

    public void setDefaultFilter(ActionState defaultFilter) {
        this.defaultFilter = defaultFilter;
    }

    public void setDefaultSort(ActionState defaultSort) {
        this.defaultSort = defaultSort;
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
}
