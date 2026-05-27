package dev.veyno.astraAH.ui.pages;

import dev.veyno.astraAH.AstraAH;
import dev.veyno.astraAH.app.PlayerDataManager;
import dev.veyno.astraAH.data.dto.Category;
import dev.veyno.astraAH.data.dto.PlayerData;
import dev.veyno.astraAH.ui.Page;
import dev.veyno.astraAH.ui.PageController;
import dev.veyno.astraAH.util.ClickableInventory;
import dev.veyno.astraAH.util.InteractiveDialogGui;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class CategoryEditPage implements Page {

    /** Materials shown as quick-select buttons in the editor dialog. */
    private static final List<Material> QUICK_MATERIALS = List.of(
            Material.DIAMOND,
            Material.IRON_INGOT,
            Material.GOLD_INGOT,
            Material.EMERALD,
            Material.NETHERITE_INGOT,
            Material.REDSTONE
    );

    private final AstraAH plugin;
    private UUID playerId;
    private final PageController pageController;

    private ClickableInventory inventory;
    private ClickableInventory.InventoryRegion content, navigation;

    public CategoryEditPage(PageController pageController, UUID playerId, AstraAH plugin) {
        this.pageController = pageController;
        this.playerId = playerId;
        this.plugin = plugin;
        buildOnce();
    }


    @Override
    public void show() {
        inventory.open();
    }

    @Override
    public void reload() {
        renderContent();
        content.refresh();
    }

    @Override
    public void invalidate(Section section) {
        if (section == Section.CONTENT || section == Section.ALL) {
            renderContent();
            content.refresh();
        }
    }

    @Override
    public void buildOnce() {
        inventory = new ClickableInventory(plugin.getInventoryManager(), Component.text("placeholder - edit categories") ,Bukkit.getPlayer(playerId));
        content = inventory.createRegionFromCoords("content",0,0,8,4);
        navigation = inventory.createRegionFromCoords("navbar", 0,5,8,5);
        renderNavBar();
        renderContent();
    }

    @Override
    public Component getPageTitle() {
        return inventory.getTitle();
    }

    private void renderContent(){
        content.clearItems();
        PlayerData playerData = plugin.getPlayerDataController().getPlayerData(playerId);
        for(Category c : playerData.getPreferences().getCategories()){
            content.addItem(
                    c.getPreview(),
                    action -> openCategoryDialog(action.getPlayer(), c, false)
            );
        }
    }

    private void renderNavBar(){
        navigation.setItem(
                0,
                new ItemStack(Material.ARROW),
                action -> {
                    content.previousPageAndRefresh();
                }
        );
        navigation.setItem(
                2,
                new ItemStack(Material.SPECTRAL_ARROW),
                action -> {
                    pageController.back();
                }
        );
        navigation.setItem(
                4,
                new ItemStack(Material.FURNACE),
                action -> openCategoryDialog(
                        action.getPlayer(),                        new Category(new ArrayList<>(), new ItemStack(Material.STONE)),
                        true
                )
        );
        navigation.setItem(
                8,
                new ItemStack(Material.ARROW),
                action -> {
                    content.nextPageAndRefresh();
                }
        );
    }

    // ---------------------------------------------------------------------
    // Category editor dialog
    // ---------------------------------------------------------------------

    /**
     * Entry point for the editor. Reads the current state of the given
     * category into a mutable working draft and opens the dialog.
     *
     * @param category the category to edit (for {@code isNew} a fresh, not yet
     *                  stored instance)
     * @param isNew    whether the category still needs to be added to the
     *                 player's preferences on save
     */
    private void openCategoryDialog(Player player, Category category, boolean isNew) {
        List<String> filters = category.getFilter() == null
                ? new ArrayList<>()
                : new ArrayList<>(category.getFilter());
        if (filters.isEmpty()) {
            filters.add("");
        }

        ItemStack preview = category.getPreview();
        Material material = preview != null ? preview.getType() : Material.STONE;
        String name = readPreviewName(preview);

        showCategoryDialog(player, category, isNew, filters, name, material);
    }

    /**
     * (Re)builds and shows the editor dialog for the given working state.
     * <p>
     * Dialogs are immutable once shown, so every interaction (adding a field,
     * picking a material) re-reads the current inputs and rebuilds the dialog
     * from scratch with the updated draft.
     * <p>
     * Field order in the dialog: filter fields first, then the preview item's
     * name and material fields, which therefore always sit at the bottom.
     */
    private void showCategoryDialog(Player player,
                                    Category category,
                                    boolean isNew,
                                    List<String> filters,
                                    String name,
                                    Material material) {

        InteractiveDialogGui dialog = InteractiveDialogGui.create(
                Component.text(isNew ? "Create category" : "Edit category"));

        // One editable text field per filter entry of the category's List<String>.
        for (int i = 0; i < filters.size(); i++) {
            dialog.input(
                    DialogInput.text("filter_" + i, Component.text("Filter " + (i + 1) + ":"))
                            .initial(filters.get(i) == null ? "" : filters.get(i))
                            .maxLength(128)
                            .build()
            );
        }

        // Preview item name + material: always kept at the bottom.
        dialog.input(
                DialogInput.text("name", Component.text("Preview name:"))
                        .initial(name == null ? "" : name)
                        .maxLength(64)
                        .build()
        );
        dialog.input(
                DialogInput.text("material", Component.text("Preview material:"))
                        .initial(material == null ? "" : material.name())
                        .maxLength(64)
                        .build()
        );

        int fieldCount = filters.size();
        List<ActionButton> buttons = new ArrayList<>();

        // 6 quick-select material buttons.
        for (Material quick : QUICK_MATERIALS) {
            buttons.add(InteractiveDialogGui.actionButton(
                    Component.text(quick.name()),
                    Component.text("Set preview material to " + quick.name()),
                    (Consumer<InteractiveDialogGui.DialogContext>) ctx -> {
                        List<String> current = readFilters(ctx, fieldCount);
                        String currentName = readName(ctx, name);
                        showCategoryDialog(player, category, isNew, current, currentName, quick);
                    }
            ));
        }

        // Button to add another filter text field.
        buttons.add(InteractiveDialogGui.actionButton(
                Component.text("+ Add filter field"),
                Component.text("Adds another filter text field"),
                (Consumer<InteractiveDialogGui.DialogContext>) ctx -> {
                    List<String> current = readFilters(ctx, fieldCount);
                    current.add("");
                    String currentName = readName(ctx, name);
                    Material chosen = readMaterial(ctx, material);
                    showCategoryDialog(player, category, isNew, current, currentName, chosen);
                }
        ));

        // Save button: persist the category and refresh the page.
        buttons.add(InteractiveDialogGui.actionButton(
                Component.text("Save"),
                Component.text("Save this category"),
                (Consumer<InteractiveDialogGui.DialogContext>) ctx -> {
                    List<String> current = readFilters(ctx, fieldCount);
                    String currentName = readName(ctx, name);
                    Material chosen = readMaterial(ctx, material);
                    saveCategory(player, category, isNew, current, currentName, chosen);
                }
        ));

        dialog.multiAction(buttons);
        dialog.show(player);
    }

    /**
     * Reads the {@code filter_0..n} text inputs from the dialog response.
     * Falls back to empty strings when no view is available.
     */
    private List<String> readFilters(InteractiveDialogGui.DialogContext ctx, int count) {
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String value = ctx.hasView() ? ctx.getText("filter_" + i) : null;
            result.add(value == null ? "" : value);
        }
        return result;
    }

    /**
     * Reads the {@code name} text input. Returns {@code fallback} when no view
     * is available.
     */
    private String readName(InteractiveDialogGui.DialogContext ctx, String fallback) {
        String raw = ctx.hasView() ? ctx.getText("name") : null;
        return raw == null ? fallback : raw;
    }

    /**
     * Reads the {@code material} text input and resolves it to a {@link Material}.
     * Returns {@code fallback} when the input is empty or not a valid material.
     */
    private Material readMaterial(InteractiveDialogGui.DialogContext ctx, Material fallback) {
        String raw = ctx.hasView() ? ctx.getText("material") : null;
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        Material parsed = Material.matchMaterial(raw.trim());
        return parsed != null ? parsed : fallback;
    }

    /** Extracts the plain-text display name from a preview item, or "" if none. */
    private String readPreviewName(ItemStack preview) {
        if (preview == null || !preview.hasItemMeta()) {
            return "";
        }
        ItemMeta meta = preview.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(meta.displayName());
    }

    /**
     * Applies the working draft to the category and persists it through the
     * player data manager. Blank filter fields are not stored, so clearing a
     * field effectively removes that entry.
     */
    private void saveCategory(Player player,
                              Category category,
                              boolean isNew,
                              List<String> filters,
                              String name,
                              Material material) {

        List<String> cleaned = new ArrayList<>();
        for (String f : filters) {
            if (f != null && !f.isBlank()) {
                cleaned.add(f.trim());
            }
        }

        Bukkit.getLogger().info(filters.toString() + " | " + cleaned.toString());


        Material chosen = material != null ? material : Material.STONE;
        ItemStack preview = new ItemStack(chosen);
        if (name != null && !name.isBlank()) {
            ItemMeta meta = preview.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(name.trim()));
                preview.setItemMeta(meta);
            }
        }

        category.setFilter(cleaned);
        category.setPreview(preview);

        PlayerDataManager.PreferencesManager preferences = plugin.getPlayerDataController()
                .getPlayerDataManager()
                .getPreferencesManager(playerId);

        if (isNew) {
            preferences.addCategory(category);
        } else {
            // Rebuild the list so the (mutated) category is re-persisted.
            List<Category> updated = new ArrayList<>(
                    plugin.getPlayerDataController()
                            .getPlayerData(playerId)
                            .getPreferences()
                            .getCategories()
            );
            preferences.setCategories(updated);
        }

        renderContent();
        content.refresh();
        show();
        player.sendMessage(Component.text(isNew ? "Category created." : "Category saved."));
    }

}