package com.dreamingfish.gridinventory.common.raid.runtime;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidExtractionActivation(
        String id,
        String node,
        BlockPos localPos,
        double radius,
        String displayName,
        List<String> tags) {
    public RaidExtractionActivation {
        id = id == null ? "" : id;
        node = node == null ? "" : node;
        localPos = localPos == null ? BlockPos.ZERO : localPos;
        radius = radius > 0 ? radius : 3.0D;
        displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
