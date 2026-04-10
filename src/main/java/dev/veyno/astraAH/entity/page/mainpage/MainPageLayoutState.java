package dev.veyno.astraAH.entity.page.mainpage;

import dev.veyno.astraAH.ah.SortType;
import org.bukkit.Material;

import java.util.List;

public class MainPageLayoutState {
    private ButtonLayout advancedCategories;
    private ButtonLayout advancedHistory;
    private SortType sortType;
    private List<Material> filter;

    private int categoryScrollIndex;
    private int historyScrollIndex;
    private int listingsPageIndex;

    private MainPageButtonLayout buttonLayout;

    public MainPageLayoutState(ButtonLayout advancedCategories, ButtonLayout advancedHistory, SortType sortType, List<Material> filter, int categoryScrollIndex, int historyScrollIndex, int listingsPageIndex, MainPageButtonLayout buttonLayout) {
        this.advancedCategories = advancedCategories;
        this.advancedHistory = advancedHistory;
        this.sortType = sortType;
        this.filter = filter;
        this.categoryScrollIndex = categoryScrollIndex;
        this.historyScrollIndex = historyScrollIndex;
        this.listingsPageIndex = listingsPageIndex;
        this.buttonLayout = buttonLayout;
    }

    public int getCategoryScrollIndex() {
        return categoryScrollIndex;
    }

    public int getHistoryScrollIndex() {
        return historyScrollIndex;
    }

    public ButtonLayout isShowAdvancedCategories() {
        return advancedCategories;
    }

    public ButtonLayout isShowAdvancedHistory() {
        return advancedHistory;
    }

    public SortType getSortType() {
        return sortType;
    }

    public List<Material> getFilter() {
        return filter;
    }

    public int getListingsPageIndex() {
        return listingsPageIndex;
    }

    public MainPageButtonLayout getButtonLayout() {
        return buttonLayout;
    }

    public enum ButtonLayout {
        DISABLED,
        BUTTON,
        SIDEBAR
    }

    public ButtonLayout getAdvancedCategories() {
        return advancedCategories;
    }

    public ButtonLayout getAdvancedHistory() {
        return advancedHistory;
    }

    public void setFilter(List<Material> filter) {
        this.filter = filter;
    }

    public void setCategoryScrollIndex(int categoryScrollIndex) {
        if(categoryScrollIndex<0) categoryScrollIndex = 0;
        this.categoryScrollIndex = categoryScrollIndex;
    }

    public void setAdvancedCategories(ButtonLayout advancedCategories) {
        this.advancedCategories = advancedCategories;
    }
}
