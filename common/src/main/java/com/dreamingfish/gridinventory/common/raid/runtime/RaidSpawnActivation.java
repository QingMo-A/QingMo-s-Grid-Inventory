package com.dreamingfish.gridinventory.common.raid.runtime;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidSpawnActivation(
        String id,
        String node,
        BlockPos localPos,
        float yaw,
        float pitch,
        String displayName,
        List<String> tags) {
    public RaidSpawnActivation {
        id = id == null ? "" : id;
        node = node == null ? "" : node;
        localPos = localPos == null ? BlockPos.ZERO : localPos;
        displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
