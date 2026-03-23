package dev.veyno.astraAH.ui.error;

import dev.veyno.astraAH.AstraAH;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class ErrorHandler {

    private final AstraAH plugin;


    public ErrorHandler(AstraAH plugin) {
        this.plugin = plugin;
    }

    public void onIllegalInventoryView(Player p){
        p.kick(MiniMessage.miniMessage().deserialize("<red>An error has occurred while rendering the requested view. If this continues to happen please try using an unmodified Minecraft client and contacting Staff"));
    }
}
