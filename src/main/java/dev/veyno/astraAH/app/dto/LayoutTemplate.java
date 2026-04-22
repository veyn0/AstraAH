package dev.veyno.astraAH.app.dto;

import org.bukkit.Material;

import java.util.List;

public class LayoutTemplate {

    private ButtonLayout advancedCategories;
    private ButtonLayout advancedHistory;
    private SortType sortType;
    private List<Material> filter;

    private boolean showSettings;
    private boolean showMyListings;
    private boolean showRefresh;
    private boolean showSort;
    private boolean showSearch;

    public LayoutTemplate(ButtonLayout advancedCategories, ButtonLayout advancedHistory, SortType sortType, List<Material> filter, boolean showSettings, boolean showMyListings, boolean showRefresh, boolean showSort, boolean showSearch) {
        this.advancedCategories = advancedCategories;
        this.advancedHistory = advancedHistory;
        this.sortType = sortType;
        this.filter = filter;
        this.showSettings = showSettings;
        this.showMyListings = showMyListings;
        this.showRefresh = showRefresh;
        this.showSort = showSort;
        this.showSearch = showSearch;
    }

    public ButtonLayout getAdvancedCategories() {
        return advancedCategories;
    }

    public ButtonLayout getAdvancedHistory() {
        return advancedHistory;
    }

    public SortType getSortType() {
        return sortType;
    }

    public List<Material> getFilter() {
        return filter;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    public boolean isShowMyListings() {
        return showMyListings;
    }

    public boolean isShowRefresh() {
        return showRefresh;
    }

    public boolean isShowSort() {
        return showSort;
    }

    public boolean isShowSearch() {
        return showSearch;
    }
}
