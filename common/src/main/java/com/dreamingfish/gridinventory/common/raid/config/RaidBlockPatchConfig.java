package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

public record RaidBlockPatchConfig(int[] pos, String block) {
    public RaidBlockPatchConfig {
        pos = pos == null ? null : pos.clone();
        block = block == null ? "" : block;
    }

    public BlockPos localBlockPos() {
        return pos != null && pos.length == 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO;
    }
}
