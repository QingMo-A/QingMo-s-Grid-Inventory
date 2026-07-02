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
        List<RaidLootItemDefinitionConfig> candidates = new ArrayList<>();
        Set<String> allowed = new HashSet<>(context.allowedCategories());
        for (RaidLootItemDefinitionConfig definition : context.definitions()) {
            if (!definition.enabled()) continue;
            if (!allowed.isEmpty() && !allowed.contains(definition.category())) continue;
            if (definition.systemValue() <= 0 || definition.item() == null) {
                warnings++;
                continue;
            }
            if (!BuiltInRegistries.ITEM.containsKey(definition.item())) {
                warnings++;
                continue;
            }
            candidates.add(definition);
        }
        if (context.pointBudget() <= 0 || context.maxStacks() <= 0 || candidates.isEmpty()) {
            return empty(context.pointBudget(), warnings);
        }

        Random random = new Random(context.lootSeed());
        List<RaidBudgetLootEntry> entries = new ArrayList<>();
        int remaining = context.pointBudget();
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
        int consumed = context.pointBudget() - remaining;
        return new RaidBudgetLootResult(entries, context.pointBudget(), consumed,
                remaining, attempts, warnings);
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

    private static RaidBudgetLootResult empty(int budget, int warnings) {
        return new RaidBudgetLootResult(List.of(), budget, 0, budget, 0, warnings);
    }
}
