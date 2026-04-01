package dev.veyno.astraAH.util;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MaterialPatternParser {

    private MaterialPatternParser() {
    }

    public static List<Material> parse(List<String> patterns) {
        Set<Material> result = new LinkedHashSet<>();

        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) {
                continue;
            }

            String upper = pattern.toUpperCase();
            if (upper.equals("*") || upper.equals("**")) {
                return List.of(Material.values());
            }

            boolean startWild = upper.startsWith("*");
            boolean endWild = upper.endsWith("*");
            String core = upper.substring(startWild ? 1 : 0, endWild ? upper.length() - 1 : upper.length());

            if (startWild && endWild) {
                for (Material material : Material.values()) {
                    if (material.name().contains(core)) {
                        result.add(material);
                    }
                }
                continue;
            }

            if (startWild) {
                for (Material material : Material.values()) {
                    if (material.name().endsWith(core)) {
                        result.add(material);
                    }
                }
                continue;
            }

            if (endWild) {
                for (Material material : Material.values()) {
                    if (material.name().startsWith(core)) {
                        result.add(material);
                    }
                }
                continue;
            }

            Material exact = Material.matchMaterial(upper);
            if (exact != null) {
                result.add(exact);
            }
        }

        return new ArrayList<>(result);
    }
}
