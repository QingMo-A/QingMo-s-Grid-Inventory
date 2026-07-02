package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.RaidLootItemDefinitionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class RaidBudgetLootGenerator {
    private RaidBudgetLootGenerator() {}

    public static RaidBudgetLootResult generate(RaidBudgetLootContext context) {
        int warnings = 0;
        List<RaidLootItemDefinitionConfig> candidates = eligibleDefinitions(
                context.definitions(), context.allowedCategories());
        int budget = context.effectiveBudget();
        int invalidDefinitions = invalidEligibleDefinitionCount(context.definitions(), context.allowedCategories());
        warnings += invalidDefinitions;
        if (budget <= 0 || context.maxStacks() <= 0 || candidates.isEmpty()) {
            return empty(budget, warnings, candidates.size());
        }

        Random random = new Random(context.lootSeed());
        List<RaidBudgetLootEntry> entries = new ArrayList<>();
        int remaining = budget;
        int attempts = 0;
        int maxAttempts = Math.max(64, context.maxStacks() * 16);
        while (remaining > 0 && entries.size() < context.maxStacks() && attempts < maxAttempts) {
            attempts++;
            RaidLootItemDefinitionConfig definition = weightedSelect(candidates, random);
            Item item = BuiltInRegistries.ITEM.get(definition.item());
            ItemStack prototype = new ItemStack(item);
            if (prototype.isEmpty()) {
                warnings++;
                continue;
            }
            int min = Math.max(1, definition.stackMin());
            int max = Math.max(min, definition.stackMax());
            int count = min + (max == min ? 0 : random.nextInt(max - min + 1));
            count = Math.min(count, prototype.getMaxStackSize());
            count = Math.min(count, remaining / definition.systemValue());
            if (count <= 0) continue;
            ItemStack stack = new ItemStack(item, count);
            if (stack.isEmpty()) {
                warnings++;
                continue;
            }
            int consumed = definition.systemValue() * count;
            entries.add(new RaidBudgetLootEntry(stack, consumed, definition));
            remaining -= consumed;
        }
        int consumed = budget - remaining;
        return new RaidBudgetLootResult(entries, budget, consumed,
                remaining, attempts, warnings, candidates.size());
    }

    public static List<RaidLootItemDefinitionConfig> eligibleDefinitions(
            List<RaidLootItemDefinitionConfig> definitions, List<String> allowedCategories) {
        List<RaidLootItemDefinitionConfig> candidates = new ArrayList<>();
        Set<String> allowed = new HashSet<>(allowedCategories == null ? List.of() : allowedCategories);
        for (RaidLootItemDefinitionConfig definition : definitions == null ? List.<RaidLootItemDefinitionConfig>of() : definitions) {
            if (!definition.enabled()) continue;
            if (!allowed.isEmpty() && !allowed.contains(definition.category())) continue;
            if (definition.systemValue() <= 0 || definition.item() == null) {
                continue;
            }
            if (!BuiltInRegistries.ITEM.containsKey(definition.item())) {
                continue;
            }
            candidates.add(definition);
        }
        return List.copyOf(candidates);
    }

    private static int invalidEligibleDefinitionCount(
            List<RaidLootItemDefinitionConfig> definitions, List<String> allowedCategories) {
        int invalid = 0;
        Set<String> allowed = new HashSet<>(allowedCategories == null ? List.of() : allowedCategories);
        for (RaidLootItemDefinitionConfig definition : definitions == null ? List.<RaidLootItemDefinitionConfig>of() : definitions) {
            if (!definition.enabled()) continue;
            if (!allowed.isEmpty() && !allowed.contains(definition.category())) continue;
            if (definition.systemValue() <= 0 || definition.item() == null
                    || !BuiltInRegistries.ITEM.containsKey(definition.item())) {
                invalid++;
            }
        }
        return invalid;
    }

    private static RaidLootItemDefinitionConfig weightedSelect(
            List<RaidLootItemDefinitionConfig> candidates, Random random) {
        int total = candidates.stream()
                .mapToInt(RaidLootItemDefinitionConfig::effectiveSpawnWeight).sum();
        int roll = random.nextInt(total);
        for (RaidLootItemDefinitionConfig definition : candidates) {
            roll -= definition.effectiveSpawnWeight();
            if (roll < 0) return definition;
        }
        return candidates.get(candidates.size() - 1);
    }

    private static RaidBudgetLootResult empty(int budget, int warnings, int candidateCount) {
        return new RaidBudgetLootResult(List.of(), budget, 0, budget, 0, warnings, candidateCount);
    }
}
