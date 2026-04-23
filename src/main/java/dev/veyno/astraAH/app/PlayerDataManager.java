package dev.veyno.astraAH.app;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.app.dto.ActionState;
import dev.veyno.astraAH.data.PlayerDataService;
import dev.veyno.astraAH.data.dto.AllowedActions;
import dev.veyno.astraAH.data.dto.Category;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.data.dto.Preferences;
import dev.veyno.astraAH.data.dto.Transaction;

import java.util.List;
import java.util.UUID;

public class PlayerDataManager {

    private final AstraAH plugin;
    private PlayerDataService playerDataService;

    public PlayerDataManager(AstraAH plugin, PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
        this.plugin = plugin;
    }

    public PlayerData getPlayerData(UUID playerId) {
        return service().getPlayerData(playerId);
    }

    public PreferencesManager getPreferencesManager(UUID playerId) {
        return new PreferencesManager(this, playerId);
    }

    public AllowedActionsManager getAllowedActionsManager(UUID playerId) {
        return new AllowedActionsManager(this, playerId);
    }

    public TransactionsManager getTransactionsManager(UUID playerId) {
        return new TransactionsManager(this, playerId);
    }

    private PlayerDataService service() {
        return playerDataService;
    }

    private boolean apply(UUID playerId, java.util.function.UnaryOperator<PlayerData> fn) {
        PlayerDataService service = service();
        PlayerData current = service.getPlayerData(playerId);
        if (current == null) return false;
        PlayerData next = fn.apply(current);
        if (next == current) return false;
        service.setPlayerData(next, false);
        return true;
    }

    public static final class PreferencesManager {

        private final PlayerDataManager outer;
        private final UUID playerId;

        private PreferencesManager(PlayerDataManager outer, UUID playerId) {
            this.outer = outer;
            this.playerId = playerId;
        }

        public Preferences get() {
            return outer.getPlayerData(playerId).getPreferences();
        }

        public boolean isShowCategories() {
            return get().isShowCategories();
        }

        public boolean isShowHistory() {
            return get().isShowHistory();
        }

        public List<Category> getCategories() {
            return get().getCategories();
        }

        public void setShowCategories(boolean value) {
            outer.apply(playerId, pd -> pd.withPreferences(pd.getPreferences().withShowCategories(value)));
        }

        public void setShowHistory(boolean value) {
            outer.apply(playerId, pd -> pd.withPreferences(pd.getPreferences().withShowHistory(value)));
        }

        public void toggleShowCategories() {
            outer.apply(playerId, pd -> pd.withPreferences(pd.getPreferences().withShowCategories(!pd.getPreferences().isShowCategories())));
        }

        public void toggleShowHistory() {
            outer.apply(playerId, pd -> pd.withPreferences(pd.getPreferences().withShowHistory(!pd.getPreferences().isShowHistory())));
        }

        public void setCategories(List<Category> categories) {
            outer.apply(playerId, pd -> pd.withPreferences(pd.getPreferences().withCategories(categories)));
        }

        public void addCategory(Category category) {
            outer.apply(playerId, pd -> pd.withPreferences(pd.getPreferences().withAddedCategory(category)));
        }
    }

    public static final class AllowedActionsManager {

        private final PlayerDataManager outer;
        private final UUID playerId;

        private AllowedActionsManager(PlayerDataManager outer, UUID playerId) {
            this.outer = outer;
            this.playerId = playerId;
        }

        public AllowedActions get() {
            return outer.getPlayerData(playerId).getAllowedActions();
        }

        public ActionState getCategories()              { return get().getCategories(); }
        public ActionState getSettings()                { return get().getSettings(); }
        public ActionState getMyListings()              { return get().getMyListings(); }
        public ActionState getRefresh()                 { return get().getRefresh(); }
        public ActionState getSort()                    { return get().getSort(); }
        public ActionState getSearch()                  { return get().getSearch(); }
        public ActionState getHistory()                 { return get().getHistory(); }
        public ActionState getShowAdvancedCategories()  { return get().getShowAdvancedCategories(); }
        public ActionState getShowAdvancedHistory()     { return get().getShowAdvancedHistory(); }
        public ActionState getReloadOnOpen()            { return get().getReloadOnOpen(); }
        public ActionState getDefaultFilter()           { return get().getDefaultFilter(); }
        public ActionState getDefaultSort()             { return get().getDefaultSort(); }

        public void setCategories(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withCategories(state)));
        }

        public void setSettings(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withSettings(state)));
        }

        public void setMyListings(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withMyListings(state)));
        }

        public void setRefresh(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withRefresh(state)));
        }

        public void setSort(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withSort(state)));
        }

        public void setSearch(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withSearch(state)));
        }

        public void setHistory(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withHistory(state)));
        }

        public void setShowAdvancedCategories(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withShowAdvancedCategories(state)));
        }

        public void setShowAdvancedHistory(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withShowAdvancedHistory(state)));
        }

        public void setReloadOnOpen(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withReloadOnOpen(state)));
        }

        public void setDefaultFilter(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withDefaultFilter(state)));
        }

        public void setDefaultSort(ActionState state) {
            outer.apply(playerId, pd -> pd.withAllowedActions(pd.getAllowedActions().withDefaultSort(state)));
        }
    }

    public static final class TransactionsManager {

        private final PlayerDataManager outer;
        private final UUID playerId;

        private TransactionsManager(PlayerDataManager outer, UUID playerId) {
            this.outer = outer;
            this.playerId = playerId;
        }

        public List<Transaction> get() {
            return outer.getPlayerData(playerId).getTransactions();
        }

        public void add(Transaction transaction) {
            outer.apply(playerId, pd -> pd.withAddedTransaction(transaction));
        }

        public void set(List<Transaction> transactions) {
            outer.apply(playerId, pd -> pd.withTransactions(transactions));
        }
    }

}