package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

public record MapBoundsConfig(int[] min, int[] max) {
    public BlockPos minPos() {
        return valid(min) && valid(max) ? new BlockPos(Math.min(min[0], max[0]), Math.min(min[1], max[1]), Math.min(min[2], max[2]))
                : BlockPos.ZERO;
    }

    public BlockPos maxPos() {
        return valid(min) && valid(max) ? new BlockPos(Math.max(min[0], max[0]), Math.max(min[1], max[1]), Math.max(min[2], max[2]))
                : BlockPos.ZERO;
    }

    public boolean contains(BlockPos pos) {
        BlockPos low = minPos();
        BlockPos high = maxPos();
        return valid(min) && valid(max)
                && pos.getX() >= low.getX() && pos.getY() >= low.getY() && pos.getZ() >= low.getZ()
                && pos.getX() <= high.getX() && pos.getY() <= high.getY() && pos.getZ() <= high.getZ();
    }

    private boolean valid(int[] value) { return value != null && value.length == 3; }
}
