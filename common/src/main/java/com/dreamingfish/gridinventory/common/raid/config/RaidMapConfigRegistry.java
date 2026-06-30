package com.dreamingfish.gridinventory.common.raid.config;

import com.dreamingfish.gridinventory.DFGridInventory;
import java.nio.file.*;
import java.util.*;

public final class RaidMapConfigRegistry {
    private static final Map<String, RaidMapConfig> MAPS = new LinkedHashMap<>();
    private static final Map<String, String> ERRORS = new LinkedHashMap<>();
    private RaidMapConfigRegistry() {}

    public static void reload(Path configRoot) {
        MAPS.clear(); ERRORS.clear();
        Path root = configRoot.resolve("df_grid_inventory/raid/maps");
        try {
            Files.createDirectories(root);
            try (var stream = Files.list(root)) {
                stream.filter(Files::isDirectory).forEach(dir -> {
                    String id = dir.getFileName().toString();
                    try {
                        RaidMapConfig map = RaidMapConfigLoader.load(dir);
                        RaidMapValidationResult validation = RaidMapConfigValidator.validate(id, map);
                        if (validation.valid()) MAPS.put(id, map);
                        else ERRORS.put(id, validation.issues().toString());
                    } catch (Exception e) {
                        ERRORS.put(id, e.getMessage());
                        DFGridInventory.LOGGER.error("Failed to load raid map {}", id, e);
                    }
                });
            }
        } catch (Exception e) { DFGridInventory.LOGGER.error("Failed to scan raid maps", e); }
    }
    public static Optional<RaidMapConfig> get(String id) { return Optional.ofNullable(MAPS.get(id)); }
    public static Collection<RaidMapConfig> all() { return List.copyOf(MAPS.values()); }
    public static Map<String, String> errors() { return Map.copyOf(ERRORS); }
}
