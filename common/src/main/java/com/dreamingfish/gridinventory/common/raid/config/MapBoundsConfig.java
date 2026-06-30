package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

public record MapBoundsConfig(int[] min, int[] max) {
    public boolean contains(BlockPos pos) {
        return min != null && max != null && min.length == 3 && max.length == 3
                && pos.getX() >= min[0] && pos.getY() >= min[1] && pos.getZ() >= min[2]
                && pos.getX() <= max[0] && pos.getY() <= max[1] && pos.getZ() <= max[2];
    }
}
