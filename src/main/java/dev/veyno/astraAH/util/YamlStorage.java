package dev.veyno.astraAH.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class YamlStorage {

    private final File file;
    private final FileConfiguration fileConfiguration;
    private final Plugin plugin;

    private final Object ioLock = new Object();

    public YamlStorage(Plugin p, String fileName, boolean copyFromResourceIfEmpty){
        this.plugin = p;
        this.file = new File(plugin.getDataFolder(), fileName);
        createFileIfNotExists(copyFromResourceIfEmpty);
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    private void createFileIfNotExists(boolean copy) {
        synchronized (ioLock) {
            if (!file.exists()) {
                try {
                    plugin.getDataFolder().mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveDataConfig() {
        synchronized (ioLock) {
            try {
                fileConfiguration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
