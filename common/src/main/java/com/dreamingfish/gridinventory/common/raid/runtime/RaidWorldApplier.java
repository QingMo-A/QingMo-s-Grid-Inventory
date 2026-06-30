package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.loot.SearchableContainerLootBinder;
import com.dreamingfish.gridinventory.common.loot.SearchableContainerLootBinding;
import com.dreamingfish.gridinventory.common.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;

public final class RaidWorldApplier {
    private RaidWorldApplier() {}
    public static int apply(ServerLevel level, RaidManifest manifest) {
        int applied = 0;
        for (ContainerAnchorActivation anchor : manifest.activeContainers()) {
            level.setBlock(anchor.blockPos(), ModBlocks.SEARCHABLE_GRID_CONTAINER.get().defaultBlockState(), 3);
            if (SearchableContainerLootBinder.bind(level, anchor.blockPos(),
                    new SearchableContainerLootBinding(manifest.raidId(), anchor.lootSeed(), manifest.mapId(),
                            anchor.zoneId(), anchor.anchorId(), anchor.containerType(), anchor.pointBudget(),
                            anchor.qualityMultiplier()), true)) applied++;
        }
        // TODO Phase 44B: clear or decorate inactive anchors.
        return applied;
    }
}
