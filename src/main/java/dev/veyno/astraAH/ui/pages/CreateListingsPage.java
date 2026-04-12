package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class CreateListingsPage implements Page {

    private AstraAH plugin;
    private UUID playerID;
    private PageController pageController;

    public CreateListingsPage(AstraAH plugin, UUID playerID, PageController pageController) {
        this.plugin = plugin;
        this.playerID = playerID;
        this.pageController = pageController;
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
