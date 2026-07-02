package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class RaidLootItemDefinitionValidator {
    private RaidLootItemDefinitionValidator() {}

    public static RaidLootItemDefinitionValidationResult validate(
            List<RaidLootItemDefinitionConfig> definitions,
            List<RaidLootItemDefinitionValidationIssue> loaderIssues) {
        List<RaidLootItemDefinitionValidationIssue> issues = new ArrayList<>(loaderIssues);
        Set<ResourceLocation> items = new HashSet<>();
        for (RaidLootItemDefinitionConfig definition : definitions) {
            String context = definition.item() == null ? "<null>" : definition.item().toString();
            if (definition.item() == null) {
                error(issues, "item must not be null");
            } else {
                if (!items.add(definition.item())) error(issues, "duplicate item: " + context);
                if (!BuiltInRegistries.ITEM.containsKey(definition.item())) {
                    warn(issues, "item is not currently registered: " + context);
                }
            }
            if (definition.category().isBlank()) error(issues, "empty category: " + context);
            if (definition.rarity().isBlank()) error(issues, "empty rarity: " + context);
            if (definition.systemValue() <= 0) error(issues, "system_value must be > 0: " + context);
            if (definition.spawnWeight() <= 0) warn(issues, "spawn_weight corrected to 1: " + context);
            if (definition.combatScore() < 0) warn(issues, "combat_score corrected to 0: " + context);
            if (definition.survivalScore() < 0) warn(issues, "survival_score corrected to 0: " + context);
            if (definition.stackMin() < 1) error(issues, "stack_min must be >= 1: " + context);
            if (definition.stackMax() < definition.stackMin()) {
                error(issues, "stack_max must be >= stack_min: " + context);
            }
        }
        return new RaidLootItemDefinitionValidationResult(issues);
    }

    public static RaidLootItemDefinitionValidationResult validate(
            List<RaidLootItemDefinitionConfig> definitions) {
        return validate(definitions, List.of());
    }

    private static void error(List<RaidLootItemDefinitionValidationIssue> issues, String message) {
        issues.add(new RaidLootItemDefinitionValidationIssue(true, message));
    }

    private static void warn(List<RaidLootItemDefinitionValidationIssue> issues, String message) {
        issues.add(new RaidLootItemDefinitionValidationIssue(false, message));
    }
}
