package dev.veyno.astraAH.data.dto;

import dev.veyno.astraAH.app.dto.ActionState;

import java.util.UUID;

public class AllowedActions {
    private final ActionState categories;
    private final ActionState settings;
    private final ActionState myListings;
    private final ActionState refresh;
    private final ActionState sort;
    private final ActionState search;
    private final ActionState history;
    private final ActionState showAdvancedCategories;
    private final ActionState showAdvancedHistory;
    private final ActionState reloadOnOpen;
    private final ActionState defaultFilter;
    private final ActionState defaultSort;

    public AllowedActions(ActionState categories,
                          ActionState settings,
                          ActionState myListings,
                          ActionState refresh,
                          ActionState sort,
                          ActionState search,
                          ActionState history,
                          ActionState showAdvancedCategories,
                          ActionState showAdvancedHistory,
                          ActionState reloadOnOpen,
                          ActionState defaultFilter,
                          ActionState defaultSort) {
        this.categories = categories;
        this.settings = settings;
        this.myListings = myListings;
        this.refresh = refresh;
        this.sort = sort;
        this.search = search;
        this.history = history;
        this.showAdvancedCategories = showAdvancedCategories;
        this.showAdvancedHistory = showAdvancedHistory;
        this.reloadOnOpen = reloadOnOpen;
        this.defaultFilter = defaultFilter;
        this.defaultSort = defaultSort;
    }

    public static AllowedActions configuredDefaults(UUID playerId) {
        //TODO: use configuration for defaults
        return new AllowedActions(
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED,
                ActionState.UNDEFINED
        );
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

    public AllowedActions withCategories(ActionState categories) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withSettings(ActionState settings) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withMyListings(ActionState myListings) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withRefresh(ActionState refresh) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withSort(ActionState sort) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withSearch(ActionState search) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withHistory(ActionState history) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withShowAdvancedCategories(ActionState showAdvancedCategories) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withShowAdvancedHistory(ActionState showAdvancedHistory) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withReloadOnOpen(ActionState reloadOnOpen) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withDefaultFilter(ActionState defaultFilter) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

    public AllowedActions withDefaultSort(ActionState defaultSort) {
        return new AllowedActions(categories, settings, myListings, refresh, sort, search, history,
                showAdvancedCategories, showAdvancedHistory, reloadOnOpen, defaultFilter, defaultSort);
    }

}