package dev.veyno.astraAH.util;
import de.leycm.i18label4j.Label;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ItemStackParser {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private ItemStackParser() {}

    /**
     * Liest einen ItemStack aus einer FileConfiguration.
     *
     * Beispiel-YAML:
     * <pre>
     * my-item:
     *   material: DIAMOND_SWORD
     *   amount: 1
     *   display-name: "<gold><bold>Mein Schwert</bold></gold>"
     *   lore:
     *     - "<gray>Erste Zeile"
     *     - "<gray>Zweite Zeile"
     *   item-model: "meinplugin:gui/back_button"
     *   unbreakable: true
     *   hide-flags: true
     *   enchantments:
     *     SHARPNESS: 5
     *     UNBREAKING: 3
     * </pre>
     *
     * Der Wert unter "item-model" muss ein vollständiger NamespacedKey sein,
     * also im Format "namespace:pfad" (z.B. "meinplugin:gui/back_button").
     * Wird kein Namespace angegeben (kein Doppelpunkt), wird der Namespace
     * des übergebenen Plugins verwendet.
     *
     * @param config die FileConfiguration
     * @param path   Pfad zum Item-Abschnitt (z.B. "my-item")
     * @param plugin das Plugin, dessen Namespace als Fallback genutzt wird
     * @return den konfigurierten ItemStack
     * @throws IllegalArgumentException wenn material fehlt oder ungültig ist
     */
    public static ItemStack parse(FileConfiguration config, String path, JavaPlugin plugin) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            throw new IllegalArgumentException("Kein Abschnitt gefunden unter: " + path);
        }
        return parseSection(section, plugin);
    }

    /**
     * Liest einen ItemStack direkt aus einem ConfigurationSection.
     * Nützlich, wenn man bereits eine Section hat (z.B. aus einer Liste).
     *
     * @param section die ConfigurationSection mit den Item-Daten
     * @param plugin  das Plugin, dessen Namespace als Fallback genutzt wird
     * @return den konfigurierten ItemStack
     */
    public static ItemStack parseSection(ConfigurationSection section, JavaPlugin plugin) {

        // --- Material (Pflichtfeld) ---
        String materialName = section.getString("material");
        if (materialName == null || materialName.isBlank()) {
            throw new IllegalArgumentException(
                    "Pflichtfeld 'material' fehlt in: " + section.getCurrentPath()
            );
        }
        Material material = Material.matchMaterial(materialName.toUpperCase());
        if (material == null || material.isAir()) {
            throw new IllegalArgumentException(
                    "Ungültiges Material '" + materialName + "' in: " + section.getCurrentPath()
            );
        }

        // --- Amount (optional, default 1) ---
        int amount = section.getInt("amount", 1);

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item; // luftige Items o.ä. haben kein Meta


        // --- Display-Name (optional) ---
        String displayName = section.getString("display-name");
//        displayName = Label.of(displayName, displayName).resolve();
        if (displayName != null && !displayName.isBlank()) {
            Component nameComponent = MM.deserialize(displayName).decoration(TextDecoration.ITALIC, false);
            meta.displayName(nameComponent);
        }

        // --- Lore (optional) ---
        List<String> rawLore = section.getStringList("lore");
//        List<String> rawLore = new ArrayList<>();
//        for(String s : rawLore){
//            rawLore.add(Label.of(s,s).resolve());
//        }
        if (!rawLore.isEmpty()) {
            List<Component> loreComponents = rawLore.stream()
                    .map(MM::deserialize)
                    .collect(Collectors.toList());
            for(Component c : loreComponents){
                c.decoration(TextDecoration.ITALIC, false);
            }
            meta.lore(loreComponents);
        }

        // --- Item Model (optional, 1.21.4+) ---
        // Erwartet einen NamespacedKey im Format "namespace:pfad".
        // Ohne Doppelpunkt wird der Plugin-Namespace verwendet.
        String itemModel = section.getString("item-model");
        if (itemModel != null && !itemModel.isBlank()) {
            NamespacedKey key = parseNamespacedKey(itemModel, plugin);
            meta.setItemModel(key);
        }

        // --- Unbreakable (optional) ---
        if (section.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
        }

        // --- Alle ItemFlags verstecken, z.B. Enchantment-Glanz (optional) ---
        if (section.getBoolean("hide-flags", false)) {
            meta.addItemFlags(ItemFlag.values());
        }

        // --- Enchantments (optional) ---
        ConfigurationSection enchSection = section.getConfigurationSection("enchantments");
        if (enchSection != null) {
            for (String key : enchSection.getKeys(false)) {
                Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                if (enchantment == null) continue; // unbekannte Verzauberung überspringen
                int level = enchSection.getInt(key, 1);
                // true = unsafe (erlaubt Level > Vanilla-Max)
                meta.addEnchant(enchantment, level, true);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Parst einen NamespacedKey aus einem String.
     * Format "namespace:pfad" wird direkt übernommen.
     * Format "pfad" (ohne Doppelpunkt) nutzt den Namespace des Plugins.
     *
     * @param raw    der rohe String aus der Config
     * @param plugin das Plugin als Namespace-Fallback
     * @return den geparsten NamespacedKey
     * @throws IllegalArgumentException bei ungültigem Format
     */
    private static NamespacedKey parseNamespacedKey(String raw, JavaPlugin plugin) {
        if (raw.contains(":")) {
            String[] parts = raw.split(":", 2);
            return new NamespacedKey(parts[0], parts[1]);
        }
        // Kein Namespace angegeben → Plugin-Namespace als Fallback
        return new NamespacedKey(plugin, raw);
    }
}