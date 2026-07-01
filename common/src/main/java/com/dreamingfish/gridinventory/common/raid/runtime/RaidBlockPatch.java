package com.dreamingfish.gridinventory.common.raid.runtime;

import net.minecraft.core.BlockPos;

public record RaidBlockPatch(BlockPos localPos, String blockState) {
    public RaidBlockPatch {
        localPos = localPos == null ? BlockPos.ZERO : localPos;
        blockState = blockState == null ? "" : blockState;
    }
}
