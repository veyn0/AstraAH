package dev.veyno.astraAH.command;

import dev.veyno.astraAH.AstraAH;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AuctionHouseCommand implements CommandExecutor {

    private final AstraAH plugin;

    public AuctionHouseCommand(AstraAH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(commandSender instanceof Player p)) return false;

        //TODO: implement feature that allows other plugins to prevent players from opening the GUI entirely

        plugin.getPageController().openMainPage(p);

        return true;
    }
}
