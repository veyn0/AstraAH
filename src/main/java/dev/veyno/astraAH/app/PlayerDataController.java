package dev.veyno.astraAH.app;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.app.dto.ActionState;
import dev.veyno.astraAH.app.dto.ButtonLayout;
import dev.veyno.astraAH.app.dto.LayoutTemplate;
import dev.veyno.astraAH.app.dto.SortType;
import dev.veyno.astraAH.data.PlayerDataService;
import dev.veyno.astraAH.data.dto.AllowedActions;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.data.dto.Preferences;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerDataController {

    private AstraAH plugin;
    private PlayerDataService playerDataService;

    private PlayerDataManager playerDataManager;

    public PlayerDataController(AstraAH plugin, PlayerDataService playerDataService) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
        this.playerDataManager = new PlayerDataManager( plugin, playerDataService);
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public PlayerDataService getPlayerDataService() {
        return playerDataService;
    }

    public PlayerData getPlayerData(UUID playerId) {
        return playerDataService.getPlayerData(playerId);
    }


    public LayoutTemplate getLayoutTemplate(Player p) {
        PlayerData playerData = getPlayerData(p.getUniqueId());
        Preferences preferences = playerData.getPreferences();
        AllowedActions allowedActions = playerData.getAllowedActions();

        ButtonLayout categories = resolveCategoryLayout(p, preferences, allowedActions);
        ButtonLayout history = resolveHistoryLayout(p, preferences, allowedActions);

        SortType sortType = SortType.NAME_A_Z; //TODO: add configurable default sortType for individual players

        boolean showSettings = isAllowedSettings(p, allowedActions);
        boolean showMyListings = isAllowedMyListings(p, allowedActions);
        boolean showRefresh = isAllowedRefresh(p, allowedActions);
        boolean showSort = isAllowedSort(p, allowedActions);
        boolean showSearch = isAllowedSearch(p, allowedActions);

        return new LayoutTemplate(
                categories,
                history,
                sortType,
                null,
                showSettings,
                showMyListings,
                showRefresh,
                showSort,
                showSearch
        );
    }

    private ButtonLayout resolveCategoryLayout(Player p, Preferences preferences, AllowedActions allowedActions) {
        if (isExplicitlyDisabled(p, allowedActions.getCategories(), "astraah.actions.categories")) {
            return ButtonLayout.DISABLED;
        }
        return preferences.isShowCategories() ? ButtonLayout.SIDEBAR : ButtonLayout.BUTTON;
    }

    private ButtonLayout resolveHistoryLayout(Player p, Preferences preferences, AllowedActions allowedActions) {
        if (isExplicitlyDisabled(p, allowedActions.getHistory(), "astraah.actions.history")) {
            return ButtonLayout.DISABLED;
        }
        return preferences.isShowHistory() ? ButtonLayout.SIDEBAR : ButtonLayout.BUTTON;
    }

    private boolean isAllowedSearch(Player p, AllowedActions actions) {
        return resolveAllowed(p, actions.getSearch(), "astraah.actions.search");
    }

    private boolean isAllowedSort(Player p, AllowedActions actions) {
        return resolveAllowed(p, actions.getSort(), "astraah.actions.sort");
    }

    private boolean isAllowedRefresh(Player p, AllowedActions actions) {
        return resolveAllowed(p, actions.getRefresh(), "astraah.actions.refresh");
    }

    private boolean isAllowedMyListings(Player p, AllowedActions actions) {
        return resolveAllowed(p, actions.getMyListings(), "astraah.actions.my_listings");
    }

    private boolean isAllowedSettings(Player p, AllowedActions actions) {
        //NOTE: kept identical to legacy behaviour which uses the "categories" permission node here.
        //      Likely a bug in the legacy code; revisit once per-action permission nodes are finalized.
        return resolveAllowed(p, actions.getSettings(), "astraah.actions.categories");
    }

    private boolean resolveAllowed(Player p, ActionState state, String permission) {
        return switch (state) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED -> plugin.getPermissionsProvider().hasPermission(p, permission);
        };
    }

    private boolean isExplicitlyDisabled(Player p, ActionState state, String permission) {
        if (state == ActionState.FALSE) return true;
        if (state == ActionState.UNDEFINED
                && !plugin.getPermissionsProvider().hasPermission(p, permission)) {
            return true;
        }
        return false;
    }

}
