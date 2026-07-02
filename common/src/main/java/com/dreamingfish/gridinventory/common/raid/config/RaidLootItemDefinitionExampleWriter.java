package com.dreamingfish.gridinventory.common.raid.config;

import java.io.IOException;
import java.nio.file.*;

public final class RaidLootItemDefinitionExampleWriter {
    private RaidLootItemDefinitionExampleWriter() {}

    public static Path write(Path configRoot) throws IOException {
        Path path = configRoot.resolve(RaidLootItemDefinitionLoader.RELATIVE_PATH);
        Files.createDirectories(path.getParent());
        Files.writeString(path, """
                [
                  {
                    "item": "minecraft:bread",
                    "enabled": true,
                    "category": "food",
                    "rarity": "common",
                    "system_value": 80,
                    "spawn_weight": 100,
                    "combat_score": 0,
                    "survival_score": 25,
                    "stack_min": 1,
                    "stack_max": 3,
                    "tags": ["food", "early_game"]
                  },
                  {
                    "item": "minecraft:iron_ingot",
                    "enabled": true,
                    "category": "material",
                    "rarity": "uncommon",
                    "system_value": 180,
                    "spawn_weight": 60,
                    "combat_score": 0,
                    "survival_score": 0,
                    "stack_min": 1,
                    "stack_max": 4,
                    "tags": ["crafting", "material"]
                  },
                  {
                    "item": "minecraft:golden_apple",
                    "enabled": true,
                    "category": "medical",
                    "rarity": "rare",
                    "system_value": 1200,
                    "spawn_weight": 5,
                    "combat_score": 50,
                    "survival_score": 100,
                    "stack_min": 1,
                    "stack_max": 1,
                    "tags": ["medical", "high_value", "rare"]
                  }
                ]
                """, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return path;
    }
}
