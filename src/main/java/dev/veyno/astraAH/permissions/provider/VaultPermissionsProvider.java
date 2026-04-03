package dev.veyno.astraAH.permissions.provider;

import dev.veyno.astraAH.permissions.PermissionsProvider;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

public class VaultPermissionsProvider implements PermissionsProvider {

    private final Permission vaultPermission;

    public VaultPermissionsProvider(ServicesManager servicesManager) {
        RegisteredServiceProvider<Permission> rsp = servicesManager.getRegistration(Permission.class);
        if (rsp == null) {
            throw new IllegalStateException("No Vault Permission provider is registered. Make sure a permissions plugin supporting Vault is installed.");
        }
        this.vaultPermission = rsp.getProvider();
    }

    @Override
    public boolean hasPermission(Player p, String node) {
        return vaultPermission.playerHas(p.getWorld().getName(), p, node);
    }

}