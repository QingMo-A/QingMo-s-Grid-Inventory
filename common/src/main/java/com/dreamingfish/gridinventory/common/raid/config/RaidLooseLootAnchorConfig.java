package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidLooseLootAnchorConfig(
        String id, String groupId, int[] pos, String containerType, int pointBudget,
        double qualityMultiplier, int weight, boolean enabled, boolean alwaysActive,
        List<String> requiresTags, List<String> forbiddenTags, List<String> tags) {
    public RaidLooseLootAnchorConfig {
        id = id == null ? "" : id;
        groupId = groupId == null ? "" : groupId;
        pos = pos == null ? null : pos.clone();
        containerType = containerType == null ? "" : containerType;
        pointBudget = Math.max(0, pointBudget);
        requiresTags = requiresTags == null ? List.of() : List.copyOf(requiresTags);
        forbiddenTags = forbiddenTags == null ? List.of() : List.copyOf(forbiddenTags);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public BlockPos localBlockPos() {
        return pos != null && pos.length == 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO;
    }

    public int effectiveWeight() {
        return weight > 0 ? weight : 1;
    }

    public double effectiveQualityMultiplier() {
        return qualityMultiplier > 0 ? qualityMultiplier : 1.0D;
    }
}
