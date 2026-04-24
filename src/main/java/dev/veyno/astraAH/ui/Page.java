package dev.veyno.astraAH.ui;

import net.kyori.adventure.text.Component;

public interface Page {

    void show();

    void reload();

    void invalidate(Section section);

    void buildOnce();

    Component getPageTitle();

    enum Section {
        CONTENT,
        CATEGORIES,
        HISTORY,
        NAVBAR,
        ALL
    }
}