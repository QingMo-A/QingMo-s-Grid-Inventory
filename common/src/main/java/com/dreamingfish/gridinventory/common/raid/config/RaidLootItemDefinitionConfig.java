package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record RaidLootItemDefinitionConfig(
        ResourceLocation item,
        boolean enabled,
        String category,
        String rarity,
        int systemValue,
        int spawnWeight,
        int combatScore,
        int survivalScore,
        int stackMin,
        int stackMax,
        List<String> tags) {
    public RaidLootItemDefinitionConfig {
        category = category == null ? "" : category;
        rarity = rarity == null ? "" : rarity;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    // systemValue is only for Raid loot generation budget; it is not a trade price.
    public int effectiveSpawnWeight() {
        return spawnWeight > 0 ? spawnWeight : 1;
    }

    public int effectiveCombatScore() {
        return Math.max(combatScore, 0);
    }

    public int effectiveSurvivalScore() {
        return Math.max(survivalScore, 0);
    }
}
