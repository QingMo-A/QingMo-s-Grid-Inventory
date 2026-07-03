package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfig;
import com.dreamingfish.gridinventory.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public final class RaidWorldResetter {
    public static final long MAX_RESET_VOLUME = 1_000_000L;

    private RaidWorldResetter() {}

    public static RaidWorldResetResult reset(ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        BlockPos localMin = map.bounds().minPos();
        BlockPos localMax = map.bounds().maxPos();
        long sizeX = (long) localMax.getX() - localMin.getX() + 1L;
        long sizeY = (long) localMax.getY() - localMin.getY() + 1L;
        long sizeZ = (long) localMax.getZ() - localMin.getZ() + 1L;
        boolean tooLarge = sizeX <= 0 || sizeY <= 0 || sizeZ <= 0
                || sizeX > MAX_RESET_VOLUME
                || sizeY > MAX_RESET_VOLUME / Math.max(1L, sizeX)
                || sizeZ > MAX_RESET_VOLUME / Math.max(1L, sizeX * sizeY);
        long volume = tooLarge ? -1L : sizeX * sizeY * sizeZ;
        if (tooLarge) {
            DFGridInventory.LOGGER.warn(
                    "Raid reset refused mapId={} raidId={} localMin={} localMax={} volume={} maxVolume={}",
                    manifest.mapId(), manifest.raidId(), localMin, localMax, volume, MAX_RESET_VOLUME);
            return new RaidWorldResetResult(true, 0, false, false, 0, 0, 1);
        }
        RaidLooseLootCleanResult looseLoot = RaidLooseLootCleaner.clean(level, map, manifest);
        BlockPos worldMin = manifest.toWorldPos(localMin);
        BlockPos worldMax = manifest.toWorldPos(localMax);
        int cleared = 0;
        for (int x = worldMin.getX(); x <= worldMax.getX(); x++) {
            for (int y = worldMin.getY(); y <= worldMax.getY(); y++) {
                for (int z = worldMin.getZ(); z <= worldMax.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        cleared++;
                    }
                }
            }
        }
        RaidTemplateApplyResult template = RaidTemplateApplier.applyTemplate(level, map, manifest);
        int placeholders = 0;
        for (var anchor : map.containerAnchors()) {
            if (!anchor.enabled()) continue;
            level.setBlock(manifest.toWorldPos(anchor.localBlockPos()),
                    ModBlocks.RAID_CONTAINER_PLACEHOLDER.get().defaultBlockState(), 3);
            placeholders++;
        }
        return new RaidWorldResetResult(false, cleared, template.applied(), template.missing(),
                placeholders, looseLoot.entitiesCleared(), template.missing() ? 1 : 0);
    }
}
