package dev.veyno.astraAH.ui;

import dev.veyno.astraAH.entity.ui.PageLayoutState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;


public interface Page {

    void open(Player p, PageLayoutState state, Page previousPage);

    Component getPageTitle();

}
