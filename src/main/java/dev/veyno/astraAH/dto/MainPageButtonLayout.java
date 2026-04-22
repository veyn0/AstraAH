package dev.veyno.astraAH.dto;

@Deprecated(forRemoval = true)
public class MainPageButtonLayout {
    private boolean showSettings;
    private boolean showMyListings;
    private boolean showRefresh;
    private boolean showSort;
    private boolean showSearch;

    public MainPageButtonLayout(boolean showSettings, boolean showMyListings, boolean showRefresh, boolean showSort, boolean showSearch) {
        this.showSettings = showSettings;
        this.showMyListings = showMyListings;
        this.showRefresh = showRefresh;
        this.showSort = showSort;
        this.showSearch = showSearch;
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
