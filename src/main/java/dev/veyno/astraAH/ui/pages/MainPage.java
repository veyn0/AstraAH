package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.ah.configuration.config.guis.main.MainPageGuiConfiguration;
import dev.veyno.astraAH.app.dto.ButtonLayout;
import dev.veyno.astraAH.app.dto.LayoutTemplate;
import dev.veyno.astraAH.app.dto.SortType;
import dev.veyno.astraAH.data.dto.CachedListing;
import dev.veyno.astraAH.data.dto.Category;
import dev.veyno.astraAH.data.dto.Listing;
import dev.veyno.astraAH.data.dto.Transaction;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.InteractiveDialogGui;
import dev.veyno.astraAH.util.MaterialPatternParser;
import dev.veyno.astraAH.util.NumberFormat;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MainPage implements Page {

    private final AstraAH plugin;
    private final PageController pageController;
    private final UUID playerId;
    private final MainPageGuiConfiguration configuration;

    private LayoutTemplate layoutTemplate;
    private List<Material> filter;
    private SortType sortType = SortType.NAME_A_Z;
    private int categoryItemSelectedIndex = 0;
    private int historySelectedIndex = 0;
    private int sortTypeSelectedIndex = 0;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion center;
    private ClickableInventory.InventoryRegion sidebarLeft;
    private ClickableInventory.InventoryRegion sidebarRight;
    private ClickableInventory.InventoryRegion bottom;

    public MainPage(AstraAH plugin, PageController pageController, UUID playerId) {
        this.plugin = plugin;
        this.pageController = pageController;
        this.playerId = playerId;
        this.configuration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();
        buildOnce();
    }

    @Override
    public void buildOnce() {
        this.layoutTemplate = fetchLayoutTemplate();
        this.filter = layoutTemplate.getFilter();
        this.sortType = layoutTemplate.getSortType() != null ? layoutTemplate.getSortType() : SortType.NAME_A_Z;
        this.sortTypeSelectedIndex = sortType.getIndex();

        createInventory();
        renderNavbar();
        renderCenter();
        if (layoutTemplate.getAdvancedCategories() == ButtonLayout.SIDEBAR) renderCategorySidebar();
        // TODO: render history sidebar once ButtonLayout.SIDEBAR for history is supported
    }

    @Override
    public void show() {
        inventory.open();
    }

    @Override
    public void reload() {
        this.layoutTemplate = fetchLayoutTemplate();

        this.filter = layoutTemplate.getFilter();
        this.sortType = layoutTemplate.getSortType() != null ? layoutTemplate.getSortType() : SortType.NAME_A_Z;
        this.sortTypeSelectedIndex = sortType.getIndex();
        this.categoryItemSelectedIndex = 0;
        this.historySelectedIndex = 0;

        createInventory();
        renderNavbar();
        renderCenter();
        if (layoutTemplate.getAdvancedCategories() == ButtonLayout.SIDEBAR) renderCategorySidebar();
    }

    @Override
    public void invalidate(Section section) {
        switch (section) {
            case CONTENT -> {
                renderCenter();
                if (center != null) center.refresh();
            }
            case CATEGORIES -> {
                if (sidebarLeft != null) {
                    renderCategorySidebar();
                    sidebarLeft.refresh();
                }
            }
            case HISTORY -> {
                if (sidebarRight != null) {
                    // TODO: render history sidebar here once implemented
                    sidebarRight.refresh();
                }
            }
            case NAVBAR -> {
                renderNavbar();
                if (bottom != null) bottom.refresh();
            }
            case ALL -> {
                renderNavbar();
                renderCenter();
                if (bottom != null) bottom.refresh();
                if (center != null) center.refresh();
                if (sidebarLeft != null)  { renderCategorySidebar(); sidebarLeft.refresh(); }
                if (sidebarRight != null) { sidebarRight.refresh(); }
            }
        }
    }

    @Override
    public Component getPageTitle() {
        return configuration.getTitle();
    }

    private void createInventory() {
        inventory = new ClickableInventory(plugin.getInventoryManager(), configuration.getTitle(), Bukkit.getPlayer(playerId));
        center = null;
        sidebarLeft = null;
        sidebarRight = null;
        bottom = null;
    }

    private LayoutTemplate fetchLayoutTemplate() {
        Player player = Bukkit.getPlayer(playerId);
        return plugin.getPlayerDataController().getLayoutTemplate(player);
    }

    private void renderCenter() {
        int fromX = layoutTemplate.getAdvancedCategories() == ButtonLayout.SIDEBAR ? 1 : 0;
        int fromY = 0;
        int toX = layoutTemplate.getAdvancedHistory() == ButtonLayout.SIDEBAR ? 7 : 8;
        int toY = 4;

        if (center != null) inventory.removeRegion("center");
        center = inventory.createRegionFromCoords("center", fromX, fromY, toX, toY);

        List<Listing> all = new ArrayList<>();
        for (CachedListing cached : plugin.getListingController().getListings()) {
            all.add(cached.getListing());
        }
        List<Listing> listings = sortListings(all, sortType);

        for (Listing l : listings) {
            if (filter != null && !filter.contains(l.getContent().getType())) continue;
            center.addItem(getDisplayItem(l), clickContext -> {
                if (clickContext.isLeftClick()) {
                    plugin.getLogger().info(Bukkit.getPlayer(playerId).getName() + " Leftclicked Listing.");
                } else if (clickContext.isRightClick()) {
                    plugin.getLogger().info(Bukkit.getPlayer(playerId).getName() + " Rightclicked Listing.");
                    pageController.openListingInfo(l);
                }
            });
        }
    }

    private void renderCategorySidebar() {
        if (sidebarLeft != null) inventory.removeRegion("categories");
        sidebarLeft = inventory.createRegionFromCoords("categories", 0, 0, 0, 5);

        sidebarLeft.setStaticItem(
                0,
                configuration.getScrollUpCategoriesIcon(),
                action -> {
                    if (!action.isLeftClick()) return;
                    sidebarLeft.scrollByAndRefresh(-1);
                }
        );

        sidebarLeft.setStaticItem(
                5,
                configuration.getScrollDownCategoriesIcon(),
                action -> {
                    if (!action.isLeftClick()) return;
                    sidebarLeft.scrollByAndRefresh(1);
                }
        );

        for (Category entry : plugin.getPlayerDataController().getPlayerData(playerId).getPreferences().getCategories()) {
            sidebarLeft.addItem(
                    entry.getPreview(),
                    action -> {
                        if (!action.isLeftClick()) return;
                        filter = MaterialPatternParser.parse(entry.getFilter());
                        invalidate(Section.CONTENT);
                    }
            );
        }
    }

    private void renderNavbar() {
        int fromX = layoutTemplate.getAdvancedCategories() == ButtonLayout.SIDEBAR ? 1 : 0;
        int y = 5;
        int toX = layoutTemplate.getAdvancedHistory() == ButtonLayout.SIDEBAR ? 7 : 8;
        int highestSlot = toX - fromX;

        if (bottom != null) inventory.removeRegion("navbar");
        bottom = inventory.createRegionFromCoords("navbar", fromX, y, toX, y);

        bottom.setItem(
                0,
                configuration.getNavigationArrowLeft(),
                action -> {
                    if (action.isDoubleClick()) return;
                    center.previousPageAndRefresh();
                }
        );

        int index = 1;

        if (layoutTemplate.getAdvancedCategories() == ButtonLayout.BUTTON) {
            renderCategoryCycleButton(index);
            index++;
        } else if (layoutTemplate.getAdvancedCategories() == ButtonLayout.DISABLED) {
            index++;
        }

        if (layoutTemplate.isShowSettings()) {
            bottom.setItem(
                    index,
                    configuration.getSettingsIcon(),
                    action -> pageController.openSettingsPage()
            );
        }
        index++;

        if (layoutTemplate.isShowMyListings()) {
            bottom.setItem(
                    index,
                    configuration.getMyListingsIcon(),
                    action -> pageController.openMyListingsPage()
            );
        }
        index++;

        if (layoutTemplate.isShowRefresh()) {
            bottom.setItem(
                    index,
                    configuration.getRefreshIcon(),
                    action -> {
                        reload();
                        show();
                    }
            );
        }
        index++;

        if (layoutTemplate.isShowSort()) {
            renderSortButton(index);
        }
        index++;

        if (layoutTemplate.isShowSearch()) {
            bottom.setItem(
                    index,
                    configuration.getSearchIcon(),
                    action -> openSearchDialog(action.getPlayer())
            );
        }
        index++;

        if (layoutTemplate.getAdvancedHistory() == ButtonLayout.BUTTON) {
            renderHistoryCycleButton(index);
        }

        bottom.setItem(
                highestSlot,
                configuration.getNavigationArrowRight(),
                action -> {
                    if (action.isDoubleClick()) return;
                    center.nextPageAndRefresh();
                }
        );
    }

    private void renderCategoryCycleButton(int slot) {
        List<Category> categories = plugin.getPlayerDataController().getPlayerData(playerId).getPreferences().getCategories();
        bottom.setItem(
                slot,
                buildCategoryCycleIcon(categories, categoryItemSelectedIndex),
                action -> {
                    List<Category> current = plugin.getPlayerDataController().getPlayerData(playerId).getPreferences().getCategories();
                    if (action.isLeftClick()) {
                        if (current.size() > categoryItemSelectedIndex + 1) {
                            categoryItemSelectedIndex++;
                            onCategoryCycleChanged(slot, current);
                        }
                    } else if (action.isRightClick()) {
                        if (categoryItemSelectedIndex > 0) {
                            categoryItemSelectedIndex--;
                            onCategoryCycleChanged(slot, current);
                        }
                    }
                }
        );
    }

    private void onCategoryCycleChanged(int slot, List<Category> current) {
        filter = MaterialPatternParser.parse(current.get(categoryItemSelectedIndex).getFilter());
        renderCategoryCycleButton(slot);
        invalidate(Section.CONTENT);
        bottom.refresh();
    }

    private ItemStack buildCategoryCycleIcon(List<Category> categories, int selected) {
        List<Component> lore = new ArrayList<>(categories.size());
        lore.add(Component.text(" "));
        for (int i = 0; i < categories.size(); i++) {
            String name = PlainTextComponentSerializer.plainText().serialize(categories.get(i).getPreview().getItemMeta().displayName());
            String template = (i == selected) ? configuration.getCategoryEnabled() : configuration.getCategoryDisabled();
            Component line = MiniMessage.miniMessage().deserialize(template.replace("{NAME}", name)).decoration(TextDecoration.ITALIC, false);
            lore.add(i, line);
        }
        for (Component c : configuration.getCategoriesIcon().lore()) {
            lore.add(c.decoration(TextDecoration.ITALIC, false));
        }
        ItemStack result = configuration.getCategoriesIcon();
        result.lore(lore);
        return result;
    }

    private void renderSortButton(int slot) {
        bottom.setItem(
                slot,
                buildSortIcon(sortType),
                action -> {
                    if (action.isLeftClick()) {
                        if (sortTypeSelectedIndex < SortType.values().length - 1) {
                            sortTypeSelectedIndex++;
                            sortType = getSortType(sortTypeSelectedIndex);
                            renderSortButton(slot);
                            invalidate(Section.CONTENT);
                            bottom.refresh();
                        }
                    } else if (action.isRightClick()) {
                        if (sortTypeSelectedIndex > 0) {
                            sortTypeSelectedIndex--;
                            sortType = getSortType(sortTypeSelectedIndex);
                            renderSortButton(slot);
                            invalidate(Section.CONTENT);
                            bottom.refresh();
                        }
                    }
                }
        );
    }

    private SortType getSortType(int index) {
        for (SortType t : SortType.values()) {
            if (t.getIndex() == index) return t;
        }
        return null;
    }

    private ItemStack buildSortIcon(SortType type) {
        MainPageGuiConfiguration guiConfiguration = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration();
        ItemStack result = guiConfiguration.getSortIcon();

        List<Component> lore = new ArrayList<>();
        lore.add(lineFor(type, SortType.NAME_A_Z,
                guiConfiguration.getMainPageSortOptionsConfiguration().getNameAZEnabled(),
                guiConfiguration.getMainPageSortOptionsConfiguration().getNameAZDisabled()));
        lore.add(lineFor(type, SortType.NAME_Z_A,
                guiConfiguration.getMainPageSortOptionsConfiguration().getNameZAEnabled(),
                guiConfiguration.getMainPageSortOptionsConfiguration().getNameZADisabled()));
        lore.add(lineFor(type, SortType.PRICE_H_L,
                guiConfiguration.getMainPageSortOptionsConfiguration().getPriceHLEnabled(),
                guiConfiguration.getMainPageSortOptionsConfiguration().getPriceHLDisabled()));
        lore.add(lineFor(type, SortType.PRICE_L_H,
                guiConfiguration.getMainPageSortOptionsConfiguration().getPriceLHEnabled(),
                guiConfiguration.getMainPageSortOptionsConfiguration().getPriceLHDisabled()));
        lore.add(lineFor(type, SortType.PRICE_PER_PIECE_H_L,
                guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceHLEnabled(),
                guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceHLDisabled()));
        lore.add(lineFor(type, SortType.PRICE_PER_PICE_L_H,
                guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceLHEnabled(),
                guiConfiguration.getMainPageSortOptionsConfiguration().getPricePerPieceLHDisabled()));

        for (Component c : guiConfiguration.getSortIcon().lore()) {
            lore.add(c);
        }

        result.lore(lore);
        return result;
    }

    private Component lineFor(SortType current, SortType option, Component enabled, Component disabled) {
        return (current == option ? enabled : disabled).decoration(TextDecoration.ITALIC, false);
    }

    private void renderHistoryCycleButton(int slot) {
        List<Transaction> entries = plugin.getPlayerDataController().getPlayerData(playerId).getTransactions();
        bottom.setItem(
                slot,
                buildHistoryIcon(entries),
                action -> {
                    List<Transaction> current = plugin.getPlayerDataController().getPlayerData(playerId).getTransactions();
                    if (action.isLeftClick()) {
                        if (current.size() > historySelectedIndex + 1) {
                            historySelectedIndex++;
                            renderHistoryCycleButton(slot);
                            bottom.refresh();
                        }
                    } else if (action.isRightClick()) {
                        if (historySelectedIndex > 0) {
                            historySelectedIndex--;
                            renderHistoryCycleButton(slot);
                            bottom.refresh();
                        }
                    }
                }
        );
    }

    private ItemStack buildHistoryIcon(List<Transaction> entries) {
        if (entries == null) {
            entries = plugin.getPlayerDataController().getPlayerData(playerId).getTransactions();
        }

        ItemStack result = configuration.getHistoryIcon();
        List<Component> lore = new ArrayList<>(entries.size() + 1);
        lore.add(Component.text(" "));

        if (entries.isEmpty()) {
            lore.add(Component.text("No history available", TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false));
        } else {
            if (historySelectedIndex >= entries.size()) {
                historySelectedIndex = entries.size() - 1;
            }
            for (int i = 0; i < entries.size(); i++) {
                Transaction entry = entries.get(i);
                String itemName = PlainTextComponentSerializer.plainText().serialize(entry.getContent().displayName());
                boolean selected = i == historySelectedIndex;
                Component line = Component.text(selected ? "> " : "| ", TextColor.color(0xAAAAAA))
                        .append(Component.text(itemName, TextColor.color(selected ? 0x5555FF : 0xFFFFFF)))
                        .append(Component.text(" (" + NumberFormat.formatGerman(entry.getPrice()) + "$)", TextColor.color(0x555555)))
                        .decoration(TextDecoration.ITALIC, false);
                lore.add(line);
            }
        }

        if (result.lore() != null) {
            for (Component c : result.lore()) {
                lore.add(c.decoration(TextDecoration.ITALIC, false));
            }
        }

        result.lore(lore);
        return result;
    }

    private void openSearchDialog(Player p) {
        Dialog dialog = InteractiveDialogGui.create(Component.text("Search"))
                .input(
                        DialogInput.text(
                                "value",
                                Component.text("Search term:")
                        ).build()
                )
                .confirmation(
                        Component.text("OK"),
                        null,
                        ctx -> onSearch(ctx.getText("value")),
                        Component.text("Cancel"),
                        null,
                        (Consumer<InteractiveDialogGui.DialogContext>) null
                )
                .build();
        p.showDialog(dialog);
    }

    private void onSearch(String searchTerm) {
        if (searchTerm == null) return;
        String term = searchTerm.replace(" ", "_").toLowerCase();
        List<Material> result = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m.name().toLowerCase().contains(term)) {
                result.add(m);
            }
        }
        filter = result;
        invalidate(Section.CONTENT);
    }

    public List<Listing> sortListings(List<Listing> listings, SortType sortType) {
        if (sortType == null) return listings;
        return switch (sortType) {
            case NAME_A_Z -> listings.stream()
                    .sorted(Comparator.comparing(l -> l.getContent().getType().name()))
                    .toList();
            case NAME_Z_A -> listings.stream()
                    .sorted(Comparator.comparing((Listing l) -> l.getContent().getType().name()).reversed())
                    .toList();
            case PRICE_H_L -> listings.stream()
                    .sorted(Comparator.comparingDouble(Listing::getPrice).reversed())
                    .toList();
            case PRICE_L_H -> listings.stream()
                    .sorted(Comparator.comparingDouble(Listing::getPrice))
                    .toList();
            case PRICE_PER_PIECE_H_L -> listings.stream()
                    .sorted(Comparator.comparingDouble((Listing l) -> -(l.getPrice() / l.getContent().getAmount())))
                    .toList();
            case PRICE_PER_PICE_L_H -> listings.stream()
                    .sorted(Comparator.comparingDouble(l -> l.getPrice() / l.getContent().getAmount()))
                    .toList();
        };
    }

    //TODO: add support for all placeholders from config.yml
    private ItemStack getDisplayItem(Listing l) {
        ItemStack result = l.getContent().clone();
        ItemMeta meta = result.getItemMeta();
        String itemName = PlainTextComponentSerializer.plainText().serialize(l.getContent().displayName());
        String displayName = plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getListingDisplayConfiguration().getNameTemplate();
        meta.customName(MiniMessage.miniMessage().deserialize(displayName
                .replace("{PRICE}", NumberFormat.formatGerman(l.getPrice()))
                .replace("{ITEM_NAME}", itemName)).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        for (String line : plugin.getConfiguration().getConfiguredGuis().getMainPageGuiConfiguration().getListingDisplayConfiguration().getLoreHeaderTemplates()) {
            String resolvedLine = line
                    .replace("{PRICE}", NumberFormat.formatGerman(l.getPrice()))
                    .replace("{ITEM_NAME}", itemName);
            lore.add(MiniMessage.miniMessage().deserialize(resolvedLine).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        result.setItemMeta(meta);
        return result;
    }
}