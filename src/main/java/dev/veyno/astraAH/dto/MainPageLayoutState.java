package dev.veyno.astraAH.dto;

import dev.veyno.astraAH.ui.SortType;
import org.bukkit.Material;

import java.util.List;

public class MainPageLayoutState {
    private ButtonLayout advancedCategories;
    private ButtonLayout advancedHistory;
    private SortType sortType;
    private List<Material> filter;


    private MainPageButtonLayout buttonLayout;

    public MainPageLayoutState(ButtonLayout advancedCategories, ButtonLayout advancedHistory, SortType sortType, List<Material> filter, MainPageButtonLayout buttonLayout) {
        this.advancedCategories = advancedCategories;
        this.advancedHistory = advancedHistory;
        this.sortType = sortType;
        this.filter = filter;
        this.buttonLayout = buttonLayout;
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

}
