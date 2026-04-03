package dev.veyno.astraAH.permissions;

import org.bukkit.entity.Player;

public interface PermissionsProvider {

    boolean hasPermission(Player p, String node);

}
