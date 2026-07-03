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
        return applyFull(level, map, manifest);
    }

    public static RaidWorldApplyResult applyFull(ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        RaidTemplateApplyResult template = RaidTemplateApplier.applyTemplate(level, map, manifest);
        RaidVariantApplyResult variants = RaidVariantApplier.apply(level, manifest);
        return applyResources(level, map, manifest, template, variants);
    }

    public static RaidWorldApplyResult applyGeneratedStateOnly(
            ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        RaidVariantApplyResult variants = RaidVariantApplier.apply(level, manifest);
        return applyResources(level, map, manifest, skippedTemplate(), variants);
    }

    public static RaidWorldApplyResult applyResourcesOnly(
            ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        // The caller has already restored/pasted the template; only activate manifest-driven resources.
        return applyResources(level, map, manifest, skippedTemplate(), new RaidVariantApplyResult(0, 0));
    }

    private static RaidWorldApplyResult applyResources(
            ServerLevel level, RaidMapConfig map, RaidManifest manifest, RaidTemplateApplyResult template,
            RaidVariantApplyResult variants) {
        int placed = 0;
        int rebound = 0;
        int cleared = 0;
        int warnings = variants.warnings();
        if (template.missing()) warnings++;
        for (ContainerAnchorActivation anchor : manifest.activeContainers()) {
            var worldPos = manifest.toWorldPos(anchor.localPos());
            var oldBlock = level.getBlockState(worldPos).getBlock();
            if (oldBlock == ModBlocks.SEARCHABLE_GRID_CONTAINER.get()) {
                rebound++;
            } else {
                placed++;
                if (oldBlock != ModBlocks.RAID_CONTAINER_PLACEHOLDER.get() && oldBlock != Blocks.AIR) {
                    warnings++;
                    DFGridInventory.LOGGER.warn(
                            "Raid active anchor replacing unexpected block mapId={} raidId={} anchorId={} localPos={} worldPos={} pasteOrigin={} oldBlock={}",
                            manifest.mapId(), manifest.raidId(), anchor.anchorId(), anchor.localPos(), worldPos,
                            manifest.pasteOrigin(), oldBlock);
                }
            }
            level.setBlock(worldPos, ModBlocks.SEARCHABLE_GRID_CONTAINER.get().defaultBlockState(), 3);
            if (!SearchableContainerLootBinder.bind(level, worldPos,
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
            var localPos = anchor.localBlockPos();
            var worldPos = manifest.toWorldPos(localPos);
            var state = level.getBlockState(worldPos);
            if (state.getBlock() == ModBlocks.RAID_CONTAINER_PLACEHOLDER.get()) {
                level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 3);
                cleared++;
            } else if (state.getBlock() == ModBlocks.SEARCHABLE_GRID_CONTAINER.get()
                    && level.getBlockEntity(worldPos) instanceof SearchableGridContainerBlockEntity container
                    && anchor.id().equals(container.getAnchorId())) {
                level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 3);
                cleared++;
            } else if (!state.isAir()) {
                warnings++;
                DFGridInventory.LOGGER.warn(
                        "Raid inactive anchor expected placeholder mapId={} raidId={} anchorId={} localPos={} worldPos={} pasteOrigin={} oldBlock={}",
                        manifest.mapId(), manifest.raidId(), anchor.id(), localPos, worldPos,
                        manifest.pasteOrigin(), state.getBlock());
            }
        }
        // TODO Phase 45C: apply structure variants for large local map changes.
        // TODO Phase 50A: allocate unique pasteOrigin and/or isolated dimension per RaidInstance.
        RaidLooseLootCleanResult cleaned = RaidLooseLootCleaner.clean(level, map, manifest);
        RaidLooseLootApplyResult looseLoot = RaidLooseLootApplier.apply(level, map, manifest);
        return new RaidWorldApplyResult(template.applied(), template.missing(), variants.patchesApplied(),
                placed, rebound, cleared, cleaned.entitiesCleared(), looseLoot.anchorsApplied(), looseLoot.stacksSpawned(),
                looseLoot.consumedBudget(), warnings + looseLoot.warnings());
    }

    private static RaidTemplateApplyResult skippedTemplate() {
        return new RaidTemplateApplyResult(false, false);
    }
}
