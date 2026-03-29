package dev.veyno.astraAH.util;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Packet-based ClickableInventory using PacketEvents.
 * No Bukkit inventories or Bukkit inventory events are used.
 * All inventory display and interaction is handled purely via packets.
 */
public class ClickableInventory {

    private static final int SLOTS_PER_ROW = 9;
    // Container type IDs for chest-like inventories (MC protocol)
    // 0 = generic 9x1, 1 = generic 9x2, ... 5 = generic 9x6
    private static final int CONTAINER_TYPE_BASE = 0;

    private final InventoryManager manager;
    private final Component title;
    private final Player player;

    private final Map<String, InventoryRegion> regions;
    private final Map<Integer, Consumer<ClickContext>> slotActions;

    private int rows;
    /** The virtual container ID assigned to this inventory. */
    private int containerId = -1;
    /** State ID for packet synchronization. */
    private int stateId = 0;
    /** The virtual slot contents (size = rows * 9). */
    private ItemStack[] contents;

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

    // ── Region management (unchanged API) ──────────────────────────────

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

    public InventoryRegion createRegion(String id, InventoryLayout layout) {
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

    // ── Open / Close / Render ──────────────────────────────────────────

    public void open() {
        if (player == null || !player.isOnline()) {
            return;
        }

        manager.registerInventory(player.getUniqueId(), this);
        containerId = manager.nextContainerId();
        buildContents();
        sendOpenWindow();
        sendWindowItems();
    }

    public void close() {
        if (player != null && player.isOnline()) {
            sendCloseWindow();
            manager.unregisterInventory(player.getUniqueId());
            containerId = -1;
        }
    }

    public void rerender() {
        if (player == null || !player.isOnline() || containerId == -1) {
            return;
        }
        buildContents();
        sendWindowItems();
    }

    public void rerenderIfOpen() {
        if (player == null || containerId == -1) {
            return;
        }
        rerender();
    }

    // ── Packet sending ─────────────────────────────────────────────────

    private void sendOpenWindow() {
        int containerType = CONTAINER_TYPE_BASE + (rows - 1); // 0=9x1 .. 5=9x6
        WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow(
                containerId,
                containerType,
                title
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private void sendWindowItems() {
        stateId++;
        List<ItemStack> items = new ArrayList<>(Arrays.asList(contents));

        // The WindowItems packet also expects the 36 player inventory slots appended
        // after the container slots. We send empty stacks for those to not interfere
        // with the player's real inventory – the client merges them.
        for (int i = 0; i < 36; i++) {
            items.add(ItemStack.EMPTY);
        }

        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(
                containerId,
                stateId,
                items,
                ItemStack.EMPTY // carried item (cursor)
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private void sendCloseWindow() {
        WrapperPlayServerCloseWindow packet = new WrapperPlayServerCloseWindow(containerId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    // ── Content building ───────────────────────────────────────────────

    private void buildContents() {
        int size = rows * SLOTS_PER_ROW;
        contents = new ItemStack[size];
        Arrays.fill(contents, ItemStack.EMPTY);
        slotActions.clear();

        for (InventoryRegion region : regions.values()) {
            region.renderInto(contents, slotActions);
        }
    }

    protected int getContainerId() {
        return containerId;
    }

    protected int getInventorySize() {
        return rows * SLOTS_PER_ROW;
    }

    // ── Click handling (called from InventoryManager packet listener) ──

    protected void handleClick(Player clicker, int rawSlot, int button, int mode) {
        // Always deny the click by re-sending the window items (resets client state)
        sendWindowItems();

        if (rawSlot < 0 || rawSlot >= getInventorySize()) {
            return;
        }

        Consumer<ClickContext> action = slotActions.get(rawSlot);
        if (action == null) {
            return;
        }

        // Derive click properties from the protocol mode + button
        boolean leftClick = (mode == 0 && button == 0) || (mode == 1 && button == 0);
        boolean rightClick = (mode == 0 && button == 1) || (mode == 1 && button == 1);
        boolean shiftClick = (mode == 1);
        boolean middleClick = (mode == 3 && button == 2);
        boolean dropClick = (mode == 4);
        boolean numberKey = (mode == 2);
        boolean doubleClick = (mode == 6);

        ClickContext context = new ClickContext(
                clicker,
                rawSlot,
                contents[rawSlot],
                shiftClick,
                leftClick,
                rightClick,
                middleClick,
                dropClick,
                numberKey,
                doubleClick,
                button,
                mode
        );

        // Run action on the region scheduler (Folia-safe)
        Bukkit.getRegionScheduler().run(manager.getPlugin(), clicker.getLocation(), task -> action.accept(context));
    }

    // ── Static helper to create Bukkit ItemStacks (unchanged) ──────────

    public static org.bukkit.inventory.ItemStack createItem(Material material, String name, String... lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    // ── Conversion: Bukkit ItemStack → PacketEvents ItemStack ──────────

    /**
     * Converts a Bukkit ItemStack to a PacketEvents ItemStack
     * using the SpigotConversionUtil provided by PacketEvents.
     */
    public static ItemStack toPacketItem(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem == null || bukkitItem.getType() == Material.AIR) {
            return ItemStack.EMPTY;
        }

        return SpigotConversionUtil.fromBukkitItemStack(bukkitItem);
    }

    /**
     * Converts a PacketEvents ItemStack back to a Bukkit ItemStack.
     */
    public static org.bukkit.inventory.ItemStack toBukkitItem(ItemStack packetItem) {
        if (packetItem == null || packetItem.isEmpty()) {
            return new org.bukkit.inventory.ItemStack(Material.AIR);
        }
        return SpigotConversionUtil.toBukkitItemStack(packetItem);
    }

    // ════════════════════════════════════════════════════════════════════
    // Inner Classes
    // ════════════════════════════════════════════════════════════════════

    public static class InventoryRegion {

        private final ClickableInventory parent;
        private final String id;
        private final List<Integer> slots;
        private final List<ClickableItem> items;

        /** Static items pinned to specific region-slot indices (key = region slot index 0..n). */
        private final Map<Integer, ClickableItem> staticItems;

        private org.bukkit.inventory.ItemStack fillerItem;
        private int firstItemIndex;

        private InventoryRegion(ClickableInventory parent, String id, List<Integer> slots) {
            this.parent = parent;
            this.id = id;
            this.slots = new ArrayList<>();
            this.items = new ArrayList<>();
            this.staticItems = new HashMap<>();
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

        /**
         * Returns a list of region-slot indices that are NOT occupied by a static item.
         * These are the slots available for dynamically paged items.
         */
        private List<Integer> getDynamicSlotIndices() {
            List<Integer> dynamic = new ArrayList<>();
            for (int i = 0; i < slots.size(); i++) {
                if (!staticItems.containsKey(i)) {
                    dynamic.add(i);
                }
            }
            return dynamic;
        }

        /**
         * Returns the number of region slots available for dynamic items.
         */
        public int getDynamicSlotCount() {
            return slots.size() - staticItems.size();
        }

        public int getLastVisibleItemIndexExclusive() {
            return Math.min(firstItemIndex + getDynamicSlotCount(), items.size());
        }

        public boolean hasNextPage() {
            return firstItemIndex + getDynamicSlotCount() < items.size();
        }

        public boolean hasPreviousPage() {
            return firstItemIndex > 0;
        }

        public InventoryRegion nextPage() {
            return scrollBy(getDynamicSlotCount());
        }

        public InventoryRegion nextPageAndRefresh() {
            nextPage();
            refresh();
            return this;
        }

        public InventoryRegion previousPage() {
            return scrollBy(-getDynamicSlotCount());
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
            return setFirstItemIndex(page * getDynamicSlotCount());
        }

        public InventoryRegion openPageAndRefresh(int page) {
            openPage(page);
            refresh();
            return this;
        }

        public int getCurrentPage() {
            int dynCount = getDynamicSlotCount();
            return dynCount <= 0 ? 0 : firstItemIndex / dynCount;
        }

        public int getMaxPages() {
            int dynCount = getDynamicSlotCount();
            if (dynCount <= 0) return 1;
            return Math.max(1, (int) Math.ceil((double) items.size() / dynCount));
        }

        public org.bukkit.inventory.ItemStack getFillerItem() {
            return fillerItem == null ? null : fillerItem.clone();
        }

        public InventoryRegion setFillerItem(org.bukkit.inventory.ItemStack fillerItem) {
            this.fillerItem = fillerItem == null ? null : fillerItem.clone();
            return this;
        }

        public InventoryRegion setFillerItem(Material material, String name, String... lore) {
            this.fillerItem = createItem(material, name, lore);
            return this;
        }

        // ── Static items ───────────────────────────────────────────────

        /**
         * Sets a static item pinned to a specific region slot index.
         * This item stays at this position regardless of scrolling/paging.
         * Dynamic items will skip this slot entirely.
         *
         * @param regionSlot the slot index within this region (0-based, NOT the inventory slot)
         * @param itemStack  the item to display
         * @param action     the click action
         * @return this region for chaining
         */
        public InventoryRegion setStaticItem(int regionSlot, org.bukkit.inventory.ItemStack itemStack, Consumer<ClickContext> action) {
            if (regionSlot < 0 || regionSlot >= slots.size()) {
                throw new IndexOutOfBoundsException(
                        "Region-Slot " + regionSlot + " ungültig, Region '" + id + "' hat " + slots.size() + " Slots"
                );
            }
            staticItems.put(regionSlot, new ClickableItem(itemStack, action));
            return this;
        }

        /**
         * Sets a static item pinned to a specific region slot index with a simple Runnable action.
         */
        public InventoryRegion setStaticItem(int regionSlot, org.bukkit.inventory.ItemStack itemStack, Runnable action) {
            return setStaticItem(regionSlot, itemStack, ctx -> action.run());
        }

        /**
         * Removes the static item at the given region slot index.
         */
        public InventoryRegion removeStaticItem(int regionSlot) {
            staticItems.remove(regionSlot);
            return this;
        }

        /**
         * Returns the static item at the given region slot index, or null if none.
         */
        public ClickableItem getStaticItem(int regionSlot) {
            return staticItems.get(regionSlot);
        }

        /**
         * Returns true if a static item occupies the given region slot index.
         */
        public boolean hasStaticItem(int regionSlot) {
            return staticItems.containsKey(regionSlot);
        }

        /**
         * Removes all static items from this region.
         */
        public InventoryRegion clearStaticItems() {
            staticItems.clear();
            return this;
        }

        /**
         * Returns an unmodifiable view of all static items (key = region slot index).
         */
        public Map<Integer, ClickableItem> getStaticItems() {
            return Collections.unmodifiableMap(staticItems);
        }

        // ── Dynamic items (unchanged API) ──────────────────────────────

        public InventoryRegion addItem(org.bukkit.inventory.ItemStack itemStack, Consumer<ClickContext> action) {
            items.add(new ClickableItem(itemStack, action));
            return this;
        }

        public InventoryRegion addItem(org.bukkit.inventory.ItemStack itemStack, Runnable action) {
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

        public InventoryRegion setItem(int index, org.bukkit.inventory.ItemStack itemStack, Consumer<ClickContext> action) {
            if (index < 0) {
                throw new IndexOutOfBoundsException("Index darf nicht negativ sein: " + index);
            }
            while (items.size() <= index) {
                items.add(null);
            }
            items.set(index, new ClickableItem(itemStack, action));
            return this;
        }

        public InventoryRegion setItem(int index, org.bukkit.inventory.ItemStack itemStack, Runnable action) {
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

        /**
         * Renders this region's items into the packet-level contents array.
         * Static items are placed first at their pinned positions.
         * Dynamic items fill the remaining slots, respecting paging/scrolling.
         */
        protected void renderInto(ItemStack[] contents, Map<Integer, Consumer<ClickContext>> slotActions) {
            ItemStack packetFiller = toPacketItem(fillerItem);

            // 1) Render static items at their pinned region-slot positions
            for (Map.Entry<Integer, ClickableItem> entry : staticItems.entrySet()) {
                int regionSlotIndex = entry.getKey();
                ClickableItem staticItem = entry.getValue();
                int inventorySlot = slots.get(regionSlotIndex);

                contents[inventorySlot] = toPacketItem(staticItem.getItemStack());
                slotActions.put(inventorySlot, staticItem.getAction());
            }

            // 2) Collect the region-slot indices that are NOT occupied by static items
            List<Integer> dynamicSlotIndices = getDynamicSlotIndices();

            // 3) Fill dynamic slots with paged items
            int dynamicItemCursor = firstItemIndex;
            for (int dynamicIdx : dynamicSlotIndices) {
                int inventorySlot = slots.get(dynamicIdx);

                if (dynamicItemCursor >= 0 && dynamicItemCursor < items.size()) {
                    ClickableItem clickableItem = items.get(dynamicItemCursor);

                    if (clickableItem != null) {
                        contents[inventorySlot] = toPacketItem(clickableItem.getItemStack());
                        slotActions.put(inventorySlot, clickableItem.getAction());
                    } else {
                        contents[inventorySlot] = fillerItem != null ? packetFiller : ItemStack.EMPTY;
                        slotActions.remove(inventorySlot);
                    }
                } else {
                    contents[inventorySlot] = fillerItem != null ? packetFiller : ItemStack.EMPTY;
                    slotActions.remove(inventorySlot);
                }

                dynamicItemCursor++;
            }
        }
    }

    // ── ClickContext ────────────────────────────────────────────────────

    public static class ClickContext {

        private final Player player;
        private final int slot;
        private final ItemStack clickedItem; // PacketEvents ItemStack
        private final boolean shiftClick;
        private final boolean leftClick;
        private final boolean rightClick;
        private final boolean middleClick;
        private final boolean dropClick;
        private final boolean numberKey;
        private final boolean doubleClick;
        private final int button;
        private final int mode;

        public ClickContext(Player player, int slot, ItemStack clickedItem,
                            boolean shiftClick, boolean leftClick, boolean rightClick,
                            boolean middleClick, boolean dropClick, boolean numberKey,
                            boolean doubleClick, int button, int mode) {
            this.player = player;
            this.slot = slot;
            this.clickedItem = clickedItem;
            this.shiftClick = shiftClick;
            this.leftClick = leftClick;
            this.rightClick = rightClick;
            this.middleClick = middleClick;
            this.dropClick = dropClick;
            this.numberKey = numberKey;
            this.doubleClick = doubleClick;
            this.button = button;
            this.mode = mode;
        }

        public Player getPlayer() {
            return player;
        }

        public int getSlot() {
            return slot;
        }

        /**
         * Returns the clicked item as a PacketEvents ItemStack.
         */
        public ItemStack getClickedItem() {
            return clickedItem;
        }

        /**
         * Returns the clicked item converted back to a Bukkit ItemStack.
         */
        public org.bukkit.inventory.ItemStack getClickedBukkitItem() {
            return toBukkitItem(clickedItem);
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
            return middleClick;
        }

        public boolean isDropClick() {
            return dropClick;
        }

        public boolean isNumberKey() {
            return numberKey;
        }

        public boolean isDoubleClick() {
            return doubleClick;
        }

        /**
         * Raw protocol button value.
         */
        public int getButton() {
            return button;
        }

        /**
         * Raw protocol inventory action mode.
         */
        public int getMode() {
            return mode;
        }
    }

    // ── ClickableItem (unchanged, still uses Bukkit ItemStack externally) ──

    public static class ClickableItem {

        private final org.bukkit.inventory.ItemStack itemStack;
        private final Consumer<ClickContext> action;

        public ClickableItem(org.bukkit.inventory.ItemStack itemStack, Consumer<ClickContext> action) {
            this.itemStack = itemStack.clone();
            this.action = action;
        }

        public org.bukkit.inventory.ItemStack getItemStack() {
            return itemStack.clone();
        }

        public Consumer<ClickContext> getAction() {
            return action;
        }
    }

    // ── InventoryManager (PacketEvents listener instead of Bukkit Listener) ──

    public static class InventoryManager extends PacketListenerAbstract {

        private final JavaPlugin plugin;
        private final Map<UUID, ClickableInventory> activeInventories;
        private final AtomicInteger containerIdCounter;

        public InventoryManager(JavaPlugin plugin) {
            super(PacketListenerPriority.HIGH);
            this.plugin = plugin;
            this.activeInventories = new ConcurrentHashMap<>();
            // Start at a high number to avoid collisions with vanilla container IDs
            this.containerIdCounter = new AtomicInteger(100);

            PacketEvents.getAPI().getEventManager().registerListener(this);
        }

        public JavaPlugin getPlugin() {
            return plugin;
        }

        /**
         * Generates a unique container ID for a new virtual inventory.
         */
        protected int nextContainerId() {
            int id = containerIdCounter.incrementAndGet();
            // Wrap around if it gets too high (container IDs are typically bytes/varints)
            if (id > 200) {
                containerIdCounter.set(100);
                return 100;
            }
            return id;
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

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            // Handle window click packets
            if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
                WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
                Player player = (Player) event.getPlayer();
                if (player == null) return;

                ClickableInventory inv = activeInventories.get(player.getUniqueId());
                if (inv == null) return;

                // Check if this click is for our virtual container
                if (packet.getWindowId() != inv.getContainerId()) return;

                // Cancel the packet – the server must not process this click
                event.setCancelled(true);

                int rawSlot = packet.getSlot();
                int button = packet.getButton();
                int mode = packet.getWindowClickType().ordinal();

                inv.handleClick(player, rawSlot, button, mode);
            }

            // Handle window close packets
            if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
                WrapperPlayClientCloseWindow packet = new WrapperPlayClientCloseWindow(event);
                Player player = (Player) event.getPlayer();
                if (player == null) return;

                ClickableInventory inv = activeInventories.get(player.getUniqueId());
                if (inv == null) return;

                if (packet.getWindowId() == inv.getContainerId()) {
                    activeInventories.remove(player.getUniqueId());
                    inv.containerId = -1;
                }
            }
        }

        /**
         * Call this in your plugin's onDisable() to clean up.
         */
        public void shutdown() {
            for (Map.Entry<UUID, ClickableInventory> entry : activeInventories.entrySet()) {
                ClickableInventory inv = entry.getValue();
                if (inv.player != null && inv.player.isOnline() && inv.containerId != -1) {
                    inv.sendCloseWindow();
                }
            }
            activeInventories.clear();
            PacketEvents.getAPI().getEventManager().unregisterListener(this);
        }
    }

    // ── Layouts (unchanged) ────────────────────────────────────────────

    public static interface InventoryLayout {
        List<Integer> getSlots(int rows);
    }

    public static class LayoutCenter implements InventoryLayout {

        @Override
        public List<Integer> getSlots(int rows) {
            List<Integer> result = new ArrayList<>();
            if (rows <= 2) return result;
            for (int i = 1; i < (rows - 1); i++) {
                for (int j = 1; j < 8; j++) {
                    result.addAll(List.of((i * 9) + j));
                }
            }
            return result;
        }
    }

    public static class LayoutSide implements InventoryLayout {

        private final boolean leftSide;

        public LayoutSide(boolean leftSide) {
            this.leftSide = leftSide;
        }

        @Override
        public List<Integer> getSlots(int rows) {
            int before = leftSide ? 0 : 8;
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                result.add((9 * i) + before);
            }
            return result;
        }
    }

    public static class LayoutHorizontalNoSides implements InventoryLayout {

        private int row;

        public LayoutHorizontalNoSides(int row) {
            this.row = row;
        }

        @Override
        public List<Integer> getSlots(int rows) {
            List<Integer> result = new ArrayList<>();
            if (row > rows) row = rows;
            int base = ((rows - 1) * 9) + 1;
            for (int i = 0; i < 7; i++) {
                result.add(i + base);
            }
            return result;
        }
    }
}