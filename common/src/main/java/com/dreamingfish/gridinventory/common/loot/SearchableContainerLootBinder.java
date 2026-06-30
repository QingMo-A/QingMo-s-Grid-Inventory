package com.dreamingfish.gridinventory.common.loot;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class SearchableContainerLootBinder {
    private SearchableContainerLootBinder() {
    }

    public static boolean bind(Level level, BlockPos pos, SearchableContainerLootBinding binding,
                               boolean resetLootGenerated) {
        if (!(level.getBlockEntity(pos) instanceof SearchableGridContainerBlockEntity container)) {
            return false;
        }
        container.applyLootBinding(binding, resetLootGenerated);
        return true;
    }
}
