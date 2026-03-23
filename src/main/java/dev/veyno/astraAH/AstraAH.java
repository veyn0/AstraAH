package dev.veyno.astraAH;

import dev.veyno.astraAH.ui.error.ErrorHandler;
import dev.veyno.astraAH.ui.UIController;
import dev.veyno.astraAH.util.ClickableInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;


/*

TODO:
 - Exclude specific items, including beeing able to match names

 */

public final class AstraAH extends JavaPlugin {

    private File dataFile;
    private FileConfiguration dataConfig;

    private UIController uiController;

    private ErrorHandler errorHandler;

    private ClickableInventory.InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        createDataFile();
        saveDefaultConfig();
        inventoryManager = new ClickableInventory.InventoryManager(this);
        errorHandler = new ErrorHandler(this);
        uiController = new UIController(this);
    }

    @Override
    public void onDisable() {
    }

    public ClickableInventory.InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public UIController getUiController() {
        return uiController;
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            try {
                getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }


    public void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
