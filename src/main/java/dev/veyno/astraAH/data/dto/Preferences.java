package dev.veyno.astraAH.data.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Preferences {
    private final boolean showCategories;
    private final boolean showHistory;
    private final List<Category> categories;

    public Preferences(boolean showCategories, boolean showHistory, List<Category> categories) {
        this.showCategories = showCategories;
        this.showHistory = showHistory;
        this.categories = categories == null ? List.of() : List.copyOf(categories);
    }

    public static Preferences configuredDefaults(UUID playerId) {
        //TODO: use defaults from configuration
        return new Preferences(
                false,
                false,
                Category.configuredDefaults(playerId)
        );
    }

    public boolean isShowCategories() {
        return showCategories;
    }

    public boolean isShowHistory() {
        return showHistory;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Preferences withShowCategories(boolean showCategories) {
        return new Preferences(showCategories, showHistory, categories);
    }

    public Preferences withShowHistory(boolean showHistory) {
        return new Preferences(showCategories, showHistory, categories);
    }

    public Preferences withCategories(List<Category> categories) {
        return new Preferences(showCategories, showHistory, categories);
    }

    public Preferences withAddedCategory(Category category) {
        List<Category> updated = new ArrayList<>(categories);
        updated.add(category);
        return withCategories(updated);
    }

}