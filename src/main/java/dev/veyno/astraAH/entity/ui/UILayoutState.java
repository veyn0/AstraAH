package dev.veyno.astraAH.entity.ui;

import dev.veyno.astraAH.ah.SortType;
import org.bukkit.Material;

import java.util.List;

public class UILayoutState {
    private boolean showAdvancedCategories;
    private boolean showAdvancedHistory;
    private SortType sortType;
    private List<Material> filter;

    private int categoryScrollIndex;
    private int historyScrollIndex;

    public UILayoutState(boolean showAdvancedCategories, boolean showAdvancedHistory, SortType sortType, List<Material> filter, int categoryScrollIndex, int historyScrollIndex) {
        this.showAdvancedCategories = showAdvancedCategories;
        this.showAdvancedHistory = showAdvancedHistory;
        this.sortType = sortType;
        this.filter = filter;
        this.categoryScrollIndex = categoryScrollIndex;
        this.historyScrollIndex = historyScrollIndex;
    }

    public int getCategoryScrollIndex() {
        return categoryScrollIndex;
    }

    public int getHistoryScrollIndex() {
        return historyScrollIndex;
    }

    public boolean isShowAdvancedCategories() {
        return showAdvancedCategories;
    }

    public boolean isShowAdvancedHistory() {
        return showAdvancedHistory;
    }

    public SortType getSortType() {
        return sortType;
    }

    public List<Material> getFilter() {
        return filter;
    }
}
