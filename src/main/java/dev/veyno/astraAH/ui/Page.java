package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.entity.page.mainpage.MainPageLayoutState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;


public interface Page {

    void open(Page previousPage);

    Component getPageTitle();

    void rebuild();

    void refresh();
}
