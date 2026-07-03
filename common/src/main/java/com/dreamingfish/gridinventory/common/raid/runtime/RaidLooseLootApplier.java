package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Map;
import java.util.stream.Collectors;

public final class RaidLooseLootApplier {
    private RaidLooseLootApplier() {}

    public static RaidLooseLootApplyResult apply(
            ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        Map<String, RaidContainerTypeConfig> types = map.containerTypes().stream()
                .collect(Collectors.toMap(RaidContainerTypeConfig::id, type -> type, (a, b) -> a));
        int anchors = 0, stacks = 0, consumed = 0, warnings = 0;
        for (RaidLooseLootActivation activation : manifest.activeLooseLoot()) {
            RaidContainerTypeConfig type = types.get(activation.containerType());
            if (type == null) { warnings++; continue; }
            int maxStacks = Math.min(3, Math.max(1, type.columns() * type.rows()));
            RaidBudgetLootResult generated = RaidBudgetLootGenerator.generate(new RaidBudgetLootContext(
                    map.id(), type.id(), activation.pointBudget(), activation.qualityMultiplier(),
                    activation.lootSeed(), maxStacks, type.allowedCategories(),
                    RaidLootItemDefinitionRegistry.enabled()));
            warnings += generated.warnings();
            if (!generated.generatedAny()) { warnings++; continue; }
            anchors++;
            consumed += generated.consumedBudget();
            var pos = manifest.toWorldPos(activation.localPos());
            for (RaidBudgetLootEntry entry : generated.entries()) {
                if (entry.stack().isEmpty()) continue;
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.2D,
                        pos.getZ() + 0.5D, entry.stack().copy());
                entity.setDefaultPickUpDelay();
                RaidLooseLootCleaner.mark(entity, manifest, activation);
                if (level.addFreshEntity(entity)) stacks++;
                else warnings++;
            }
        }
        return new RaidLooseLootApplyResult(anchors, stacks, consumed, warnings);
    }
}
