package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.RaidLootItemDefinitionConfig;

import java.util.List;

public record RaidBudgetLootContext(
        String mapId,
        String containerTypeId,
        int pointBudget,
        long lootSeed,
        int maxStacks,
        List<String> allowedCategories,
        List<RaidLootItemDefinitionConfig> definitions) {
    public RaidBudgetLootContext {
        mapId = mapId == null ? "" : mapId;
        containerTypeId = containerTypeId == null ? "" : containerTypeId;
        pointBudget = Math.max(0, pointBudget);
        maxStacks = Math.max(0, maxStacks);
        allowedCategories = allowedCategories == null ? List.of() : List.copyOf(allowedCategories);
        definitions = definitions == null ? List.of() : List.copyOf(definitions);
    }
}
