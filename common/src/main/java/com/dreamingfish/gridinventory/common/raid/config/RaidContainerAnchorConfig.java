package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidContainerAnchorConfig(String id, String zone, String group, int[] pos, String containerType,
                                        double qualityMultiplier, int weight, boolean enabled, List<String> tags) {
    public BlockPos localBlockPos() {
        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    @Deprecated
    public BlockPos blockPos() { return localBlockPos(); }

    public int effectiveWeight() {
        return weight > 0 ? weight : 100;
    }
}
