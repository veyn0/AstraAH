package dev.veyno.astraAH.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PlayerPreferences(
        UUID playerId,
        List<PlayerPreferencesCategoryEntry> categoryEntries,
        boolean showCategories,
        boolean showHistory
) {

    public PlayerPreferences {
        categoryEntries = categoryEntries == null ? List.of() : List.copyOf(categoryEntries);
    }

    public PlayerPreferences(UUID playerId) {

        //TODO: use configured defaults

        this(playerId, List.of(), true, true);
    }

    public PlayerPreferences withCategoryEntries(List<PlayerPreferencesCategoryEntry> categoryEntries) {
        return new PlayerPreferences(playerId, categoryEntries, showCategories, showHistory);
    }

    public PlayerPreferences withAddedCategory(PlayerPreferencesCategoryEntry categoryEntry) {
        List<PlayerPreferencesCategoryEntry> updatedEntries = new ArrayList<>(categoryEntries);
        updatedEntries.add(categoryEntry);
        return withCategoryEntries(updatedEntries);
    }


}
