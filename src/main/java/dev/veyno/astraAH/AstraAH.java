package dev.veyno.astraAH;

import dev.veyno.astraAH.ui.ErrorHandler;
import dev.veyno.astraAH.ui.UIController;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstraAH extends JavaPlugin {

    private UIController uiController;

    private ErrorHandler errorHandler;

    @Override
    public void onEnable() {
        errorHandler = new ErrorHandler(this);
        uiController = new UIController(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public UIController getUiController() {
        return uiController;
    }
}
