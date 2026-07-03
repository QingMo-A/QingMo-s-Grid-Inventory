package com.dreamingfish.gridinventory.common.raid.runtime;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidLooseLootActivation(
        String anchorId, String groupId, BlockPos localPos, String containerType,
        int pointBudget, double qualityMultiplier, long lootSeed, List<String> tags) {
    public RaidLooseLootActivation {
        anchorId = anchorId == null ? "" : anchorId;
        groupId = groupId == null ? "" : groupId;
        localPos = localPos == null ? BlockPos.ZERO : localPos;
        containerType = containerType == null ? "" : containerType;
        pointBudget = Math.max(0, pointBudget);
        qualityMultiplier = qualityMultiplier > 0 ? qualityMultiplier : 1.0D;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
