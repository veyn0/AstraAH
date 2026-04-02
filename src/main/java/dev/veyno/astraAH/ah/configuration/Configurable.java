package dev.veyno.astraAH.ah.configuration;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.util.ItemStackParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Configurable {

    private final String path;
    private final AstraAH plugin;

    public Configurable(String path, AstraAH plugin) {
        this.path = path;
        this.plugin = plugin;
    }

    public AstraAH getPlugin() {
        return plugin;
    }

    public String getPath() {
        return path;
    }

    protected FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    protected ConfigurationSection getSection(String subPath) {
        ConfigurationSection section = getConfig().getConfigurationSection(resolvePath(subPath));
        if (section == null) {
            throw new IllegalArgumentException("Missing configuration section: " + resolvePath(subPath));
        }
        return section;
    }

    protected String resolvePath(String subPath) {
        if (subPath == null || subPath.isBlank()) {
            return path;
        }
        return path + "." + subPath;
    }

    public ItemStack getItem(String subPath){
        return ItemStackParser.parseSection(getSection("items." + subPath), plugin);
    }

    public Component getMessage(String subPath){
        return MiniMessage.miniMessage().deserialize(getString(subPath, ""));
    }

    public String getString(String subPath) {
        return getConfig().getString(resolvePath(subPath));
    }

    public String getString(String subPath, String defaultValue) {
        return getConfig().getString(resolvePath(subPath), defaultValue);
    }

    public boolean getBoolean(String subPath) {
        return getConfig().getBoolean(resolvePath(subPath));
    }

    public boolean getBoolean(String subPath, boolean defaultValue) {
        return getConfig().getBoolean(resolvePath(subPath), defaultValue);
    }

    public int getInt(String subPath, int defaultValue) {
        return getConfig().getInt(resolvePath(subPath), defaultValue);
    }

    public double getDouble(String subPath, double defaultValue) {
        return getConfig().getDouble(resolvePath(subPath), defaultValue);
    }

    public List<String> getStringList(String subPath) {
        return getConfig().getStringList(resolvePath(subPath));
    }
}
