package dev.veyno.astraAH.data.dto;

import java.util.List;
import java.util.UUID;

public class Preferences {
    private boolean showCategories;
    private boolean showHistory;
    private List<Category> categories;

    public static Preferences configuredDefaults(UUID playerId){
        Preferences preferences = new Preferences();
                //TODO: make work
        preferences.setShowCategories(false);
        preferences.setShowHistory(false);
        preferences.setCategories(Category.configuredDefaults(playerId));
        return preferences;
    }

    public boolean isShowCategories() {
        return showCategories;
    }

    public void setShowCategories(boolean showCategories) {
        this.showCategories = showCategories;
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public void setShowHistory(boolean showHistory) {
        this.showHistory = showHistory;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
