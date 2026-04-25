package dev.veyno.astraAH.util;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
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
 *
 * <h2>Large mode</h2>
 * When constructed with {@code large = true}, the virtual grid is a fixed
 * {@code 9 x 10} layout: the top 6 rows map to the chest container, rows 6–8
 * map to the main player inventory (Bukkit slots 9–35), and row 9 maps to
 * the hotbar (Bukkit slots 0–8). Regions can span the full 9×10 grid; the
 * renderer transparently splits the output into the container {@code WindowItems}
 * payload and the 36 player-inventory slots that are part of the same packet.
 * <p>
 * In large mode the player inventory is NOT mirrored any more: the slots
 * covered by the virtual grid are filled with the configured
 * {@link #setPlayerSlotFillerItem(org.bukkit.inventory.ItemStack) filler item}
 * (or empty if none is set). When the inventory closes, a single
 * {@code WindowItems} packet with the player's real inventory is sent so the
 * client resyncs the actual contents.
 * <p>
 * When constructed with {@code large = false} (default) no player-inventory
 * data is sent at all when opening; the 36 trailing slots of the
 * {@code WindowItems} packet are left empty. This means the client will
 * visually clear the player inventory until something (e.g. another packet
 * or closing the container) forces a resync. Use large mode if you want
 * predictable control over those slots.
 */
public class ClickableInventory {

    private static final int SLOTS_PER_ROW = 9;
    // Container type IDs for chest-like inventories (MC protocol)
    // 0 = generic 9x1, 1 = generic 9x2, ... 5 = generic 9x6
    private static final int CONTAINER_TYPE_BASE = 0;

    /** Number of container rows exposed by a 6-row chest. */
    private static final int MAX_CONTAINER_ROWS = 6;
    /** Total rows in the virtual grid when large mode is active: 6 chest + 3 main-inv + 1 hotbar. */
    private static final int LARGE_GRID_ROWS = 10;
    /** Number of player-inventory slots appended to the WindowItems payload (27 main + 9 hotbar). */
    private static final int PLAYER_INV_SLOTS = 36;

    private final InventoryManager manager;
    private final Component title;
    private final Player player;
    private final boolean large;

    private final Map<String, InventoryRegion> regions;
    private final Map<Integer, Consumer<ClickContext>> slotActions;

    private int rows;
    /** The virtual container ID assigned to this inventory. */
    private int containerId = -1;
    /** State ID for packet synchronization. */
    private int stateId = 0;
    /** The virtual slot contents for the grid (size = grid rows * 9). */
    private ItemStack[] contents;
    /** Optional filler rendered in empty player-inventory slots (only used in large mode). */
    private org.bukkit.inventory.ItemStack playerSlotFillerItem;

    /**
     * Convenience constructor — defaults to {@code large = false} (classic 6-row chest,
     * player inventory is NOT actively managed and NOT mirrored after open).
     */
    public ClickableInventory(InventoryManager manager, Component title, Player player) {
        this(manager, title, player, false);
    }

    public ClickableInventory(InventoryManager manager, Component title, Player player, boolean large) {
        this.manager = manager;
        this.title = title;
        this.player = player;
        this.large = large;
        this.regions = new LinkedHashMap<>();
        this.slotActions = new HashMap<>();
        this.rows = MAX_CONTAINER_ROWS;
    }

    /**
     * Sets the number of rows of the chest container. Only meaningful when large mode
     * is disabled — in large mode the container is always 6 rows so the virtual 9×10
     * grid is complete.
     */
    public ClickableInventory setRows(int rows) {
        if (large) {
            throw new IllegalStateException("setRows ist im large-Mode nicht erlaubt (festes 9x10 Grid)");
        }
        if (rows < 1 || rows > MAX_CONTAINER_ROWS) {
            throw new IllegalArgumentException("Anzahl der Reihen muss zwischen 1 und 6 liegen");
        }
        this.rows = rows;
        return this;
    }

    public int getRows() {
        return rows;
    }

    /**
     * @return the total row count of the virtual grid exposed to the region API —
     * always 10 in large mode, equals {@link #getRows()} otherwise.
     */
    public int getGridRows() {
        return large ? LARGE_GRID_ROWS : rows;
    }

    public boolean isLarge() {
        return large;
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

    /**
     * Sets the item rendered in player-inventory slots that are not covered by any region.
     * Only applies in large mode. Pass {@code null} to leave those slots empty.
     */
    public ClickableInventory setPlayerSlotFillerItem(org.bukkit.inventory.ItemStack itemStack) {
        this.playerSlotFillerItem = itemStack == null ? null : itemStack.clone();
        return this;
    }

    /**
     * Sets a filler item by {@link Material}. The item will have no display name and no lore.
     * Only applies in large mode.
     */
    public ClickableInventory setPlayerSlotFillerItem(Material material) {
        if (material == null || material == Material.AIR) {
            this.playerSlotFillerItem = null;
            return this;
        }
        this.playerSlotFillerItem = new org.bukkit.inventory.ItemStack(material);
        return this;
    }

    public org.bukkit.inventory.ItemStack getPlayerSlotFillerItem() {
        return playerSlotFillerItem == null ? null : playerSlotFillerItem.clone();
    }

    // ── Coordinate helpers ─────────────────────────────────────────────

    /**
     * Converts an (x, y) coordinate to a grid slot index.
     * Origin (0,0) is the top-left corner of the inventory.
     * <p>
     * In large mode, y is valid up to 9 (6 container + 3 main inv + 1 hotbar).
     * Otherwise y is valid up to 5.
     */
    public static int slotAt(int x, int y) {
        return y * SLOTS_PER_ROW + x;
    }

    /**
     * Computes all slot indices inside the rectangular area from (x1,y1) to (x2,y2), inclusive.
     * The coordinates are automatically normalized so order doesn't matter.
     * Slots are returned in reading order (left-to-right, top-to-bottom).
     * <p>
     * This static variant keeps the legacy 0–5 y range for backwards compatibility.
     * Use {@link #slotsFromCoords(int, int, int, int, int)} for arbitrary max-y.
     */
    public static List<Integer> slotsFromCoords(int x1, int y1, int x2, int y2) {
        return slotsFromCoords(x1, y1, x2, y2, MAX_CONTAINER_ROWS - 1);
    }

    /**
     * Same as {@link #slotsFromCoords(int, int, int, int)} but lets the caller specify
     * the maximum allowed y (inclusive). Used internally to validate against the
     * instance grid size (which may extend to y=9 in large mode).
     */
    public static List<Integer> slotsFromCoords(int x1, int y1, int x2, int y2, int maxY) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxYNorm = Math.max(y1, y2);

        if (minX < 0 || maxX > 8) {
            throw new IllegalArgumentException("X-Koordinate muss zwischen 0 und 8 liegen (war: " + x1 + ", " + x2 + ")");
        }
        if (minY < 0 || maxYNorm > maxY) {
            throw new IllegalArgumentException("Y-Koordinate muss zwischen 0 und " + maxY + " liegen (war: " + y1 + ", " + y2 + ")");
        }

        List<Integer> result = new ArrayList<>((maxX - minX + 1) * (maxYNorm - minY + 1));
        for (int y = minY; y <= maxYNorm; y++) {
            for (int x = minX; x <= maxX; x++) {
                result.add(slotAt(x, y));
            }
        }
        return result;
    }

    // ── Region management ──────────────────────────────────────────────

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
        return createRegion(id, layout.getSlots(getGridRows()));
    }

    public InventoryRegion createRegion(String id, int... slots) {
        List<Integer> slotList = new ArrayList<>(slots.length);
        for (int slot : slots) {
            slotList.add(slot);
        }
        return createRegion(id, slotList);
    }

    /**
     * Creates a region spanning the rectangular area from (x1,y1) to (x2,y2).
     * Origin (0,0) is top-left. Both corners are inclusive and order-independent.
     * <p>
     * In large mode y may range from 0 to 9. Otherwise from 0 to 5.
     */
    public InventoryRegion createRegionFromCoords(String id, int x1, int y1, int x2, int y2) {
        return createRegion(id, slotsFromCoords(x1, y1, x2, y2, getGridRows() - 1));
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
            if (large) {
                // Resync the player inventory so the client sees the real server state again.
                sendRealPlayerInventory();
            }
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
        // Container packet is always a chest of `rows` rows — large mode just
        // extends the virtual grid over the 36 player-inv slots that the
        // WindowItems payload already carries.
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

        int containerSize = rows * SLOTS_PER_ROW;
        List<ItemStack> items = new ArrayList<>(containerSize + PLAYER_INV_SLOTS);

        // 1) Container slots
        if (large) {
            // In large mode the contents array is sized for the full 9x10 grid
            // but the container only uses the first 6 rows (54 slots).
            for (int i = 0; i < containerSize; i++) {
                items.add(contents[i]);
            }
        } else {
            // Classic mode: contents array is already sized to containerSize
            for (int i = 0; i < contents.length; i++) {
                items.add(contents[i]);
            }
        }

        // 2) Player-inventory slots of the WindowItems payload.
        //    The packet expects the 36 player slots appended after the container
        //    slots in protocol order: main inventory (rows 2–4) first, then hotbar.
        if (large) {
            // Large mode: render from the virtual grid.
            //   Grid rows 6,7,8 → main inventory (Bukkit slots 9–35, protocol order matches)
            //   Grid row 9     → hotbar (Bukkit slots 0–8)
            for (int i = 6 * SLOTS_PER_ROW; i < 9 * SLOTS_PER_ROW; i++) {
                items.add(contents[i]);
            }
            for (int i = 9 * SLOTS_PER_ROW; i < 10 * SLOTS_PER_ROW; i++) {
                items.add(contents[i]);
            }
        } else {
            // Non-large mode: do NOT mirror the player inventory. Send empty
            // slots — the client will visually clear the player inventory while
            // this container is open, which matches the documented contract.
            for (int i = 0; i < PLAYER_INV_SLOTS; i++) {
                items.add(ItemStack.EMPTY);
            }
        }

        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(
                containerId,
                stateId,
                items,
                ItemStack.EMPTY // carried item (cursor)
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    /**
     * Sends a {@code WindowItems} packet addressed to window id 0 (the player's
     * own inventory window) carrying the player's real inventory. Used when
     * closing a large-mode inventory so the client resyncs the real state of
     * the 36 player slots.
     */
    private void sendRealPlayerInventory() {
        org.bukkit.inventory.PlayerInventory playerInv = player.getInventory();

        // Window id 0 is the player-inventory window. Its slot layout is:
        //   0       : crafting result
        //   1–4     : crafting grid (2x2)
        //   5–8     : armor (head, chest, legs, feet)
        //   9–35    : main inventory
        //   36–44   : hotbar
        //   45      : offhand
        // Total: 46 slots.
        List<ItemStack> items = new ArrayList<>(46);
        // Crafting result + grid are not tracked server-side in the same way;
        // we send empty here, the client reconciles once any further event fires.
        for (int i = 0; i < 5; i++) {
            items.add(ItemStack.EMPTY);
        }
        // Armor slots (Bukkit order: helmet, chest, legs, boots)
        items.add(toPacketItem(playerInv.getHelmet()));
        items.add(toPacketItem(playerInv.getChestplate()));
        items.add(toPacketItem(playerInv.getLeggings()));
        items.add(toPacketItem(playerInv.getBoots()));
        // Main inventory
        for (int i = 9; i <= 35; i++) {
            items.add(toPacketItem(playerInv.getItem(i)));
        }
        // Hotbar
        for (int i = 0; i <= 8; i++) {
            items.add(toPacketItem(playerInv.getItem(i)));
        }
        // Offhand
        items.add(toPacketItem(playerInv.getItemInOffHand()));

        stateId++;
        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(
                0,
                stateId,
                items,
                ItemStack.EMPTY
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private void sendCloseWindow() {
        WrapperPlayServerCloseWindow packet = new WrapperPlayServerCloseWindow(containerId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    // ── Content building ───────────────────────────────────────────────

    private void buildContents() {
        int size = getGridRows() * SLOTS_PER_ROW;
        contents = new ItemStack[size];
        Arrays.fill(contents, ItemStack.EMPTY);
        slotActions.clear();

        for (InventoryRegion region : regions.values()) {
            region.renderInto(contents, slotActions);
        }

        if (large) {
            // Fill uncovered player-inventory slots (grid rows 6–9) with the
            // configured filler, so the player sees a deterministic layout
            // instead of whatever was in their inventory before opening.
            ItemStack packetFiller = toPacketItem(playerSlotFillerItem);
            int playerGridStart = MAX_CONTAINER_ROWS * SLOTS_PER_ROW;
            for (int i = playerGridStart; i < size; i++) {
                if (contents[i].isEmpty() && !packetFiller.isEmpty()) {
                    contents[i] = packetFiller;
                }
            }
        }
    }

    protected int getContainerId() {
        return containerId;
    }

    /**
     * Size of the virtual grid (in slots). This is what region APIs validate against —
     * in large mode it is 90, otherwise {@code rows * 9}.
     */
    protected int getInventorySize() {
        return getGridRows() * SLOTS_PER_ROW;
    }

    // ── Click handling (called from InventoryManager packet listener) ──

    /**
     * @param rawSlot slot index as reported by the client's click packet — this
     *                uses the chest's slot layout: 0 .. (rows*9 - 1) are chest
     *                slots, then (rows*9) .. (rows*9 + 26) are the 27 main-inv
     *                slots, then the next 9 are the hotbar.
     */
    protected void handleClick(Player clicker, int rawSlot, int button, int mode) {
        // Always deny the click by re-sending the window items (resets client state)
        sendWindowItems();

        // Translate the client-reported raw slot to our virtual grid index.
        int gridSlot = rawSlotToGridSlot(rawSlot);
        if (gridSlot < 0 || gridSlot >= getInventorySize()) {
            return;
        }

        Consumer<ClickContext> action = slotActions.get(gridSlot);
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
                gridSlot,
                contents[gridSlot],
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

    /**
     * Translates a raw slot from a client click packet into the virtual grid slot.
     * <p>
     * For a chest with {@code rows} rows opened on top of the player inventory, the
     * client numbers slots as:
     * <ul>
     *   <li>{@code 0 .. rows*9 - 1}  → chest slots (top-left to bottom-right)</li>
     *   <li>{@code rows*9 .. rows*9 + 26} → main inventory (rows 6,7,8 of our grid)</li>
     *   <li>next 9 slots               → hotbar (row 9 of our grid)</li>
     * </ul>
     * In non-large mode, clicks on the player inventory portion are outside the
     * grid and return {@code -1} (ignored upstream).
     */
    private int rawSlotToGridSlot(int rawSlot) {
        int containerSize = rows * SLOTS_PER_ROW;

        if (rawSlot < 0) return -1;
        if (rawSlot < containerSize) return rawSlot; // chest slot → same grid index

        if (!large) {
            return -1; // player-inv clicks are not part of the grid
        }

        int playerSlot = rawSlot - containerSize; // 0..35
        if (playerSlot < 0 || playerSlot >= PLAYER_INV_SLOTS) return -1;

        if (playerSlot < 27) {
            // Main inventory → grid rows 6,7,8
            return MAX_CONTAINER_ROWS * SLOTS_PER_ROW + playerSlot;
        } else {
            // Hotbar → grid row 9
            return 9 * SLOTS_PER_ROW + (playerSlot - 27);
        }
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

        public ClickableInventory create(Component title, Player player, boolean large) {
            return new ClickableInventory(this, title, player, large);
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

                    inv.sendRealPlayerInventory();

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
                    if (inv.large) {
                        inv.sendRealPlayerInventory();
                    }
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