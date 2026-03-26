package dev.veyno.astraAH.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class YamlStorage {

    private static Map<String, Object> fileLocks = new ConcurrentHashMap<>();

    private final File file;
    private final FileConfiguration fileConfiguration;
    private final Plugin plugin;
    private final String fileName;

    public YamlStorage(Plugin p, String fileName, boolean copyFromResourceIfEmpty, int saveIntervalSeconds){
        if(!fileLocks.containsKey(fileName)){
            fileLocks.put(fileName,new Object());
        }

        this.plugin = p;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName+".yml");
        createFileIfNotExists(copyFromResourceIfEmpty);
        this.fileConfiguration = YamlConfiguration.loadConfiguration(file);
        startSaveSchedule(saveIntervalSeconds);
    }

    private void createFileIfNotExists(boolean copy) {
        synchronized (fileLocks.get(fileName)) {
            if(copy) plugin.saveResource(file.getName(), false);
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

    private void saveFile() {
        synchronized (fileLocks.get(fileName)) {
            try {
                fileConfiguration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createFileBackup(){
        synchronized (fileLocks.get(fileName)) {
            try {
                fileConfiguration.save(new File(file.getParentFile(),"backup-"+fileName+"/backup-"+fileName+".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFileAsync(){
        Bukkit.getAsyncScheduler().runNow(plugin, task ->{
            createFileBackup();
            saveFile();
        });
    }

    private void startSaveSchedule(int seconds){
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            createFileBackup();
            saveFile();
        }, 1, seconds, TimeUnit.SECONDS);
    }


}
