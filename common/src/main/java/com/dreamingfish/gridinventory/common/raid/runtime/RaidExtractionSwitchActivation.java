package com.dreamingfish.gridinventory.common.raid.runtime;
import net.minecraft.core.BlockPos;
import java.util.List;
public record RaidExtractionSwitchActivation(String id, BlockPos localPos, double radius,
        String displayName, List<String> tags) {
    public RaidExtractionSwitchActivation {
        id = id == null ? "" : id; localPos = localPos == null ? BlockPos.ZERO : localPos;
        radius = radius > 0 ? radius : 3.0D; displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
