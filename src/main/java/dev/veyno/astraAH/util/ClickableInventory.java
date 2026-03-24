package dev.veyno.astraAH.util;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public class ClickableInventory {

    private static final int SLOTS_PER_ROW = 9;

    private final InventoryManager manager;
    private final Component title;
    private final Player player;

    private final Map<String, InventoryRegion> regions;
    private final Map<Integer, Consumer<ClickContext>> slotActions;

    private int rows;
    private Inventory inventory;

    public ClickableInventory(InventoryManager manager, Component title, Player player) {
        this.manager = manager;
        this.title = title;
        this.player = player;
        this.regions = new LinkedHashMap<>();
        this.slotActions = new HashMap<>();
        this.rows = 6;
    }

    public ClickableInventory setRows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Anzahl der Reihen muss zwischen 1 und 6 liegen");
        }
        this.rows = rows;
        return this;
    }

    public int getRows() {
        return rows;
    }

    public Component getTitle() {
        return title;
    }

    public Player getPlayer() {
        return player;
    }

    public InventoryManager getManager() {
        return manager;
    }

    public InventoryRegion createRegion(String id, List<Integer> slots) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Region id darf nicht leer sein");
        }
        if (regions.containsKey(id)) {
            throw new IllegalArgumentException("Region mit id '" + id + "' existiert bereits");
        }

        InventoryRegion region = new InventoryRegion(this, id, slots);
        regions.put(id, region);
        return region;
    }

    public InventoryRegion createRegion(String id, InventoryLayout layout){
        return createRegion(id, layout.getSlots(getRows()));
    }

    public InventoryRegion createRegion(String id, int... slots) {
        List<Integer> slotList = new ArrayList<>(slots.length);
        for (int slot : slots) {
            slotList.add(slot);
        }
        return createRegion(id, slotList);
    }

    public InventoryRegion getRegion(String id) {
        return regions.get(id);
    }

    public Collection<InventoryRegion> getRegions() {
        return Collections.unmodifiableCollection(regions.values());
    }

    public boolean hasRegion(String id) {
        return regions.containsKey(id);
    }

    public ClickableInventory removeRegion(String id) {
        regions.remove(id);
        return this;
    }

    public ClickableInventory clearRegions() {
        regions.clear();
        return this;
    }

    public void open() {
        if (player == null) {
            return;
        }

        manager.registerInventory(player.getUniqueId(), this);
        updateInventory();
        player.openInventory(inventory);
    }

    public void close() {
        if (player != null) {
            player.closeInventory();
            manager.unregisterInventory(player.getUniqueId());
        }
    }

    public void rerender() {
        updateInventory();

        if (player != null) {
            Bukkit.getRegionScheduler().run(manager.getPlugin(), player.getLocation(), task -> player.openInventory(inventory));
        }
    }

    public void rerenderIfOpen() {
        if (player == null) {
            return;
        }

        if (inventory == null) {
            return;
        }

        if (!player.getOpenInventory().getTopInventory().equals(inventory)) {
            return;
        }

        rerender();
    }

    private void updateInventory() {
        inventory = Bukkit.createInventory(null, rows * SLOTS_PER_ROW, title);
        slotActions.clear();

        for (InventoryRegion region : regions.values()) {
            region.renderInto(inventory, slotActions);
        }
    }

    protected Inventory getInventory() {
        return inventory;
    }

    protected int getInventorySize() {
        return rows * SLOTS_PER_ROW;
    }

    protected void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= getInventorySize()) {
            return;
        }

        Consumer<ClickContext> action = slotActions.get(rawSlot);
        if (action == null) {
            return;
        }

        ClickContext context = new ClickContext(
                (Player) event.getWhoClicked(),
                event.getClick(),
                event.getSlot(),
                event.getCurrentItem(),
                event.isShiftClick(),
                event.isLeftClick(),
                event.isRightClick()
        );

        Bukkit.getRegionScheduler().run(manager.getPlugin(), event.getWhoClicked().getLocation(), task -> action.accept(context));
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    public static class InventoryRegion {

        private final ClickableInventory parent;
        private final String id;
        private final List<Integer> slots;
        private final List<ClickableItem> items;

        private ItemStack fillerItem;
        private int firstItemIndex;

        private InventoryRegion(ClickableInventory parent, String id, List<Integer> slots) {
            this.parent = parent;
            this.id = id;
            this.slots = new ArrayList<>();
            this.items = new ArrayList<>();
            this.firstItemIndex = 0;

            if (slots == null || slots.isEmpty()) {
                throw new IllegalArgumentException("Eine Region muss mindestens einen Slot enthalten");
            }

            Set<Integer> uniqueSlots = new HashSet<>();
            int inventorySize = parent.getInventorySize();

            for (Integer slot : slots) {
                if (slot == null) {
                    throw new IllegalArgumentException("Slot darf nicht null sein");
                }
                if (slot < 0 || slot >= inventorySize) {
                    throw new IllegalArgumentException("Ungültiger Slot " + slot + " für Inventargröße " + inventorySize);
                }
                if (!uniqueSlots.add(slot)) {
                    throw new IllegalArgumentException("Doppelter Slot in Region '" + id + "': " + slot);
                }
                this.slots.add(slot);
            }
        }

        public String getId() {
            return id;
        }

        public ClickableInventory getParent() {
            return parent;
        }

        public List<Integer> getSlots() {
            return Collections.unmodifiableList(slots);
        }

        public List<ClickableItem> getItems() {
            return Collections.unmodifiableList(items);
        }

        public int getSlotCount() {
            return slots.size();
        }

        public int getItemCount() {
            return items.size();
        }

        public int getFirstItemIndex() {
            return firstItemIndex;
        }

        public InventoryRegion setFirstItemIndex(int firstItemIndex) {
            this.firstItemIndex = clampFirstItemIndex(firstItemIndex);
            return this;
        }

        public InventoryRegion setFirstItemIndexAndRefresh(int firstItemIndex) {
            setFirstItemIndex(firstItemIndex);
            refresh();
            return this;
        }

        public int getLastVisibleItemIndexExclusive() {
            return Math.min(firstItemIndex + slots.size(), items.size());
        }

        public boolean hasNextPage() {
            return firstItemIndex + slots.size() < items.size();
        }

        public boolean hasPreviousPage() {
            return firstItemIndex > 0;
        }

        public InventoryRegion nextPage() {
            return scrollBy(slots.size());
        }

        public InventoryRegion nextPageAndRefresh() {
            nextPage();
            refresh();
            return this;
        }

        public InventoryRegion previousPage() {
            return scrollBy(-slots.size());
        }

        public InventoryRegion previousPageAndRefresh() {
            previousPage();
            refresh();
            return this;
        }

        public InventoryRegion scrollBy(int offset) {
            return setFirstItemIndex(firstItemIndex + offset);
        }

        public InventoryRegion scrollByAndRefresh(int offset) {
            scrollBy(offset);
            refresh();
            return this;
        }

        public InventoryRegion openPage(int page) {
            if (page < 0) {
                page = 0;
            }

            return setFirstItemIndex(page * slots.size());
        }

        public InventoryRegion openPageAndRefresh(int page) {
            openPage(page);
            refresh();
            return this;
        }

        public int getCurrentPage() {
            return slots.isEmpty() ? 0 : firstItemIndex / slots.size();
        }

        public int getMaxPages() {
            return Math.max(1, (int) Math.ceil((double) items.size() / slots.size()));
        }

        public ItemStack getFillerItem() {
            return fillerItem == null ? null : fillerItem.clone();
        }

        public InventoryRegion setFillerItem(ItemStack fillerItem) {
            this.fillerItem = fillerItem == null ? null : fillerItem.clone();
            return this;
        }

        public InventoryRegion setFillerItem(Material material, String name, String... lore) {
            this.fillerItem = createItem(material, name, lore);
            return this;
        }

        public InventoryRegion addItem(ItemStack itemStack, Consumer<ClickContext> action) {
            items.add(new ClickableItem(itemStack, action));
            return this;
        }

        public InventoryRegion addItem(ItemStack itemStack, Runnable action) {
            items.add(new ClickableItem(itemStack, ctx -> action.run()));
            return this;
        }

        public InventoryRegion addItems(List<ClickableItem> clickableItems) {
            items.addAll(clickableItems);
            return this;
        }

        public InventoryRegion addItems(ClickableItem... clickableItems) {
            items.addAll(Arrays.asList(clickableItems));
            return this;
        }

        public InventoryRegion setItem(int index, ItemStack itemStack, Consumer<ClickContext> action) {
            checkItemIndex(index);
            items.set(index, new ClickableItem(itemStack, action));
            return this;
        }

        public InventoryRegion setItem(int index, ItemStack itemStack, Runnable action) {
            return setItem(index, itemStack, ctx -> action.run());
        }

        public ClickableItem getItem(int index) {
            checkItemIndex(index);
            return items.get(index);
        }

        public InventoryRegion removeItem(int index) {
            checkItemIndex(index);
            items.remove(index);
            firstItemIndex = clampFirstItemIndex(firstItemIndex);
            return this;
        }

        public InventoryRegion clearItems() {
            items.clear();
            firstItemIndex = 0;
            return this;
        }

        public InventoryRegion refresh() {
            parent.rerender();
            return this;
        }

        private int clampFirstItemIndex(int requestedIndex) {
            if (items.isEmpty()) {
                return 0;
            }

            int maxIndex = Math.max(0, items.size() - 1);
            if (requestedIndex < 0) {
                return 0;
            }
            if (requestedIndex > maxIndex) {
                return maxIndex;
            }
            return requestedIndex;
        }

        private void checkItemIndex(int index) {
            if (index < 0 || index >= items.size()) {
                throw new IndexOutOfBoundsException("Ungültiger Item-Index " + index + ", Größe: " + items.size());
            }
        }

        protected void renderInto(Inventory inventory, Map<Integer, Consumer<ClickContext>> slotActions) {
            for (int i = 0; i < slots.size(); i++) {
                int slot = slots.get(i);
                int itemIndex = firstItemIndex + i;

                if (itemIndex >= 0 && itemIndex < items.size()) {
                    ClickableItem clickableItem = items.get(itemIndex);
                    inventory.setItem(slot, clickableItem.getItemStack());
                    slotActions.put(slot, clickableItem.getAction());
                } else {
                    if (fillerItem != null) {
                        inventory.setItem(slot, fillerItem.clone());
                    } else {
                        inventory.setItem(slot, null);
                    }
                    slotActions.remove(slot);
                }
            }
        }
    }

    public static class ClickContext {

        private final Player player;
        private final ClickType clickType;
        private final int slot;
        private final ItemStack clickedItem;
        private final boolean shiftClick;
        private final boolean leftClick;
        private final boolean rightClick;

        public ClickContext(Player player, ClickType clickType, int slot, ItemStack clickedItem,
                            boolean shiftClick, boolean leftClick, boolean rightClick) {
            this.player = player;
            this.clickType = clickType;
            this.slot = slot;
            this.clickedItem = clickedItem;
            this.shiftClick = shiftClick;
            this.leftClick = leftClick;
            this.rightClick = rightClick;
        }

        public Player getPlayer() {
            return player;
        }

        public ClickType getClickType() {
            return clickType;
        }

        public int getSlot() {
            return slot;
        }

        public ItemStack getClickedItem() {
            return clickedItem;
        }

        public boolean isShiftClick() {
            return shiftClick;
        }

        public boolean isLeftClick() {
            return leftClick;
        }

        public boolean isRightClick() {
            return rightClick;
        }

        public boolean isMiddleClick() {
            return clickType == ClickType.MIDDLE;
        }

        public boolean isDropClick() {
            return clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP;
        }

        public boolean isNumberKey() {
            return clickType.isKeyboardClick();
        }

        public boolean isDoubleClick() {
            return clickType == ClickType.DOUBLE_CLICK;
        }
    }

    public static class ClickableItem {

        private final ItemStack itemStack;
        private final Consumer<ClickContext> action;

        public ClickableItem(ItemStack itemStack, Consumer<ClickContext> action) {
            this.itemStack = itemStack.clone();
            this.action = action;
        }

        public ItemStack getItemStack() {
            return itemStack.clone();
        }

        public Consumer<ClickContext> getAction() {
            return action;
        }
    }

    public static class InventoryManager implements Listener {

        private final JavaPlugin plugin;
        private final Map<UUID, ClickableInventory> activeInventories;

        public InventoryManager(JavaPlugin plugin) {
            this.plugin = plugin;
            this.activeInventories = new HashMap<>();
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        public JavaPlugin getPlugin() {
            return plugin;
        }

        protected void registerInventory(UUID playerId, ClickableInventory inventory) {
            activeInventories.put(playerId, inventory);
        }

        protected void unregisterInventory(UUID playerId) {
            activeInventories.remove(playerId);
        }

        public ClickableInventory create(Component title, Player player) {
            return new ClickableInventory(this, title, player);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            Player clicker = (Player) event.getWhoClicked();
            ClickableInventory clickableInventory = activeInventories.get(clicker.getUniqueId());

            if (clickableInventory == null) {
                return;
            }

            Inventory topInventory = event.getView().getTopInventory();
            if (!topInventory.equals(clickableInventory.getInventory())) {
                return;
            }

            clickableInventory.handleClick(event);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!(event.getPlayer() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getPlayer();
            ClickableInventory clickableInventory = activeInventories.get(player.getUniqueId());

            if (clickableInventory == null) {
                return;
            }

            if (event.getInventory().equals(clickableInventory.getInventory())) {
                activeInventories.remove(player.getUniqueId());
            }
        }
    }

    public static interface InventoryLayout{
        List<Integer> getSlots(int rows);
    }

    public static class LayoutCenter implements InventoryLayout{

        @Override
        public List<Integer> getSlots(int rows) {
            List<Integer> result = new ArrayList<>();
            if(rows<=2)return result;
            for(int i = 1; i < (rows-1); i++){
                for(int j = 1; j < 7; j++) {
                    result.addAll(List.of((i * 9)+j));
                }
            }

            return result;
        }
    }


    public static class LayoutSide implements InventoryLayout{

        private final boolean leftSide;

        public LayoutSide(boolean leftSide) {
            this.leftSide = leftSide;
        }

        @Override
        public List<Integer> getSlots(int rows) {
            int before = leftSide ? 0 : 8;
            List<Integer> result = new ArrayList<>();
            for(int i = 0; i<rows; i++){
                result.add((9*i)+before);
            }
            return result;
        }
    }

    public static class LayoutHorizontalNoSides implements InventoryLayout{

        private int row;

        public LayoutHorizontalNoSides(int row){
            this.row = row;
        }

        @Override
        public List<Integer> getSlots(int rows) {
            List<Integer> result = new ArrayList<>();

            if(row>rows) row = rows;

            for(int i = ((rows-1)*9); i < (rows*9); i++ ){
                result.add(i);
            }

            return result;
        }
    }

}