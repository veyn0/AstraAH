package dev.veyno.astraAH.permissions.provider;

import dev.veyno.astraAH.permissions.PermissionsProvider;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

public class LuckPermsPermissionsProvider implements PermissionsProvider {

    private final LuckPerms luckPerms;

    public LuckPermsPermissionsProvider() {
        this.luckPerms = LuckPermsProvider.get();
    }

    @Override
    public boolean hasPermission(Player p, String node) {
        User user = luckPerms.getUserManager().getUser(p.getUniqueId());
        if (user == null) {
            return p.hasPermission(node);
        }
        QueryOptions options = luckPerms.getContextManager().getQueryOptions(p);
        return user.getCachedData()
                .getPermissionData(options)
                .checkPermission(node)
                .asBoolean();
    }

}