package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidSpawnAnchorConfig(
        String id,
        String node,
        int[] pos,
        float yaw,
        float pitch,
        int weight,
        boolean enabled,
        boolean alwaysActive,
        List<String> requiresTags,
        List<String> forbiddenTags,
        String displayName,
        List<String> tags) {
    public RaidSpawnAnchorConfig {
        id = id == null ? "" : id;
        node = node == null ? "" : node;
        pos = pos == null ? null : pos.clone();
        requiresTags = requiresTags == null ? List.of() : List.copyOf(requiresTags);
        forbiddenTags = forbiddenTags == null ? List.of() : List.copyOf(forbiddenTags);
        displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public BlockPos localBlockPos() {
        return pos != null && pos.length == 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO;
    }

    public int effectiveWeight() {
        return weight > 0 ? weight : 1;
    }
}
