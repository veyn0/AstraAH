package dev.veyno.astraAH.permissions.provider;

import dev.veyno.astraAH.permissions.PermissionsProvider;
import org.bukkit.entity.Player;

public class DefaultPermissionsProvider implements PermissionsProvider {

    @Override
    public boolean hasPermission(Player p, String node) {
        return p.hasPermission(node);
    }

}