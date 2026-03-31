package dev.veyno.astraAH.ah.configuration;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.util.ItemStackParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Configurable {

    private final String path;
    private final AstraAH plugin;

    public Configurable(String path, AstraAH plugin) {
        this.path = path;
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getPath() {
        return path;
    }

    public ItemStack getItem(String subPath){
        return  ItemStackParser.parseSection(plugin.getConfig().getConfigurationSection(path+"."+subPath),plugin);
    }

    public Component getMessage(String subPath){
        return MiniMessage.miniMessage().deserialize(plugin.getConfig().getString(path+".items."+ subPath));
    }
}
