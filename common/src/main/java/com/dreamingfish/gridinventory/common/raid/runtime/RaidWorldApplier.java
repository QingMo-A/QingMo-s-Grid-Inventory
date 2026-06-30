package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.loot.SearchableContainerLootBinder;
import com.dreamingfish.gridinventory.common.loot.SearchableContainerLootBinding;
import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfig;
import com.dreamingfish.gridinventory.common.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;
import java.util.stream.Collectors;

public final class RaidWorldApplier {
    private RaidWorldApplier() {}
    public static RaidWorldApplyResult apply(ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        int placed = 0;
        int rebound = 0;
        int cleared = 0;
        int warnings = 0;
        for (ContainerAnchorActivation anchor : manifest.activeContainers()) {
            var oldBlock = level.getBlockState(anchor.blockPos()).getBlock();
            if (oldBlock == ModBlocks.SEARCHABLE_GRID_CONTAINER.get()) {
                rebound++;
            } else {
                placed++;
                if (oldBlock != ModBlocks.RAID_CONTAINER_PLACEHOLDER.get() && oldBlock != Blocks.AIR) {
                    warnings++;
                    DFGridInventory.LOGGER.warn(
                            "Raid active anchor replacing unexpected block mapId={} raidId={} anchorId={} pos={} oldBlock={}",
                            manifest.mapId(), manifest.raidId(), anchor.anchorId(), anchor.blockPos(), oldBlock);
                }
            }
            level.setBlock(anchor.blockPos(), ModBlocks.SEARCHABLE_GRID_CONTAINER.get().defaultBlockState(), 3);
            if (!SearchableContainerLootBinder.bind(level, anchor.blockPos(),
                    new SearchableContainerLootBinding(manifest.raidId(), anchor.lootSeed(), manifest.mapId(),
                            anchor.zoneId(), anchor.anchorId(), anchor.containerType(), anchor.pointBudget(),
                            anchor.qualityMultiplier()), true)) {
                warnings++;
            }
        }
        Set<String> activeIds = manifest.activeContainers().stream()
                .map(ContainerAnchorActivation::anchorId).collect(Collectors.toSet());
        for (var anchor : map.containerAnchors()) {
            if (!anchor.enabled() || activeIds.contains(anchor.id())) continue;
            var state = level.getBlockState(anchor.blockPos());
            if (state.getBlock() == ModBlocks.RAID_CONTAINER_PLACEHOLDER.get()) {
                level.setBlock(anchor.blockPos(), Blocks.AIR.defaultBlockState(), 3);
                cleared++;
            } else if (state.getBlock() == ModBlocks.SEARCHABLE_GRID_CONTAINER.get()
                    && level.getBlockEntity(anchor.blockPos()) instanceof SearchableGridContainerBlockEntity container
                    && anchor.id().equals(container.getAnchorId())) {
                level.setBlock(anchor.blockPos(), Blocks.AIR.defaultBlockState(), 3);
                cleared++;
            } else if (!state.isAir()) {
                warnings++;
                DFGridInventory.LOGGER.warn(
                        "Raid inactive anchor expected placeholder mapId={} raidId={} anchorId={} pos={} oldBlock={}",
                        manifest.mapId(), manifest.raidId(), anchor.id(), anchor.blockPos(), state.getBlock());
            }
        }
        // TODO Phase 45A: apply block patch variants before placing loot containers.
        // TODO Phase 45C: apply structure variants for large local map changes.
        // TODO Phase 45B: enable extraction points from RaidManifest.
        // TODO Phase 46A: spawn/static-render loose loot nodes from LooseLootManifest.
        // TODO Phase 48A: restore/reset raid instance world after raid ends.
        // TODO Phase 48B: support per-raid instance origin offsets instead of direct world coordinates.
        return new RaidWorldApplyResult(placed, rebound, cleared, warnings);
    }
}
