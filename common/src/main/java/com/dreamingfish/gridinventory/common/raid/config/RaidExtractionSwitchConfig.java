package com.dreamingfish.gridinventory.common.raid.config;
import net.minecraft.core.BlockPos;
import java.util.List;
public record RaidExtractionSwitchConfig(String id, int[] pos, double radius, boolean enabled,
        List<String> requiresTags, List<String> forbiddenTags, String displayName, List<String> tags) {
    public RaidExtractionSwitchConfig {
        id = id == null ? "" : id; pos = pos == null ? null : pos.clone();
        requiresTags = requiresTags == null ? List.of() : List.copyOf(requiresTags);
        forbiddenTags = forbiddenTags == null ? List.of() : List.copyOf(forbiddenTags);
        displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
    public BlockPos localBlockPos() { return pos != null && pos.length == 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO; }
    public double effectiveRadius() { return radius > 0 ? radius : 3.0D; }
}
