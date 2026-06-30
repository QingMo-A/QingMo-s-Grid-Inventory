package com.dreamingfish.gridinventory.common.loot;

import net.minecraft.resources.ResourceLocation;

public record ContainerLootContext(long raidId, long raidSeed, String mapId, String zoneId, String anchorId,
                                   String containerType, int zoneTier, int pointBudget,
                                   double qualityMultiplier, ResourceLocation fallbackLootTableId) {
}
