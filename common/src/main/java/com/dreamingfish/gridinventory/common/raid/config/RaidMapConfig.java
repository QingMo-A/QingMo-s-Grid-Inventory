package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record RaidMapConfig(String id, String displayName, String dimension, ResourceLocation template,
                            int[] origin, int[] defaultSpawn,
                            MapBoundsConfig bounds, int expectedPlayers,
                            int raidTimeSeconds, List<RaidZoneConfig> zones,
                            List<RaidContainerTypeConfig> containerTypes,
                            List<RaidContainerAnchorConfig> containerAnchors) {
    // TODO Phase 48B: apply origin offset to all local anchor positions for multi-instance raids.
    // TODO Phase 48C: restore placeholder blocks on raid reset.
    public RaidMapConfig {
        dimension = dimension == null || dimension.isBlank() ? "minecraft:overworld" : dimension;
        origin = valid(origin) ? origin.clone() : new int[]{0, 0, 0};
        defaultSpawn = valid(defaultSpawn) ? defaultSpawn.clone() : new int[]{0, 0, 0};
    }

    public BlockPos pasteOriginPos() { return pos(origin, BlockPos.ZERO); }
    public BlockPos defaultSpawnLocalPos() { return pos(defaultSpawn, BlockPos.ZERO); }
    public BlockPos defaultSpawnWorldPos() { return toWorldPos(defaultSpawnLocalPos()); }
    public BlockPos toWorldPos(BlockPos localPos) { return pasteOriginPos().offset(localPos); }
    public BlockPos toWorldPos(int[] localPos) { return toWorldPos(pos(localPos, BlockPos.ZERO)); }
    public BlockPos toLocalPos(BlockPos worldPos) { return worldPos.subtract(pasteOriginPos()); }

    @Deprecated
    public BlockPos originPos() { return pasteOriginPos(); }
    @Deprecated
    public BlockPos defaultSpawnPos() { return defaultSpawnLocalPos(); }

    private static boolean valid(int[] value) { return value != null && value.length == 3; }
    private static BlockPos pos(int[] value, BlockPos fallback) {
        return valid(value) ? new BlockPos(value[0], value[1], value[2]) : fallback;
    }
}
