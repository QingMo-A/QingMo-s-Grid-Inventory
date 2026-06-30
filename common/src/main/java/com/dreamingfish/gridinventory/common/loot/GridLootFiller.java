package com.dreamingfish.gridinventory.common.loot;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridExplicitInsertHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class GridLootFiller {
    private GridLootFiller() {
    }

    public static int insertAll(GridInventoryData grid, List<ItemStack> stacks) {
        int inserted = 0;
        for (ItemStack stack : stacks) {
            inserted += insertStack(grid, stack);
        }
        return inserted;
    }

    public static int insertStack(GridInventoryData grid, ItemStack stack) {
        ItemStack remainder = stack.copy();
        int inserted = 0;
        for (int y = 0; y < grid.getRows() && !remainder.isEmpty(); y++) {
            for (int x = 0; x < grid.getColumns() && !remainder.isEmpty(); x++) {
                int moved = GridExplicitInsertHelper.insertOrMergeAt(grid, remainder, x, y, false, 0);
                if (moved > 0) {
                    remainder.shrink(moved);
                    inserted += moved;
                }
            }
        }
        return inserted;
    }
}
