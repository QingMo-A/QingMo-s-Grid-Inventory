package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidNavigationNodeConfig(String id, int[] pos, List<String> tags) {
    public RaidNavigationNodeConfig {
        id = id == null ? "" : id;
        pos = pos == null ? null : pos.clone();
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public BlockPos localBlockPos() {
        return pos != null && pos.length == 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO;
    }
}
