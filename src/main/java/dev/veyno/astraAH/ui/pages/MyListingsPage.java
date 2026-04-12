package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class MyListingsPage implements Page {

    private UUID playerID;
    private AstraAH plugin;
    private PageController pageController;

    private ClickableInventory inventory;

    public MyListingsPage(UUID playerID, AstraAH plugin, PageController pageController) {
        this.playerID = playerID;
        this.plugin = plugin;
        this.pageController = pageController;
        rebuild();
    }

    @Override
    public void open(Page previousPage) {

    }

    @Override
    public Component getPageTitle() {
        return null;
    }

    @Override
    public void rebuild() {

    }

    @Override
    public void refresh() {

    }
}
