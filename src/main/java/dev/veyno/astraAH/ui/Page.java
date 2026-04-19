package dev.veyno.astraAH.ui;

import net.kyori.adventure.text.Component;


public interface Page {

    void open(Page previousPage);

    Component getPageTitle();

    void rebuild();

    void refresh();
}
