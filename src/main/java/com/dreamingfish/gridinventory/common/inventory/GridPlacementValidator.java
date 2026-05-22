package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.api.IGridInventory;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class GridPlacementValidator {
    private GridPlacementValidator() {
    }

    public static boolean canPlace(IGridInventory inventory, ItemStack stack, int targetX, int targetY, boolean rotated, @Nullable UUID ignoredEntryId) {
        if (stack.isEmpty() || stack.is(ModItems.SMALL_GRID_BAG.get())) {
            return false;
        }
        if (targetX < 0 || targetY < 0) {
            return false;
        }
        var size = GridItemSizeManager.getSize(stack);
        if (rotated && !size.rotatable()) {
            return false;
        }
        int width = size.placedWidth(rotated);
        int height = size.placedHeight(rotated);
        if (targetX + width > inventory.getColumns() || targetY + height > inventory.getRows()) {
            return false;
        }
        for (var entry : inventory.getEntries()) {
            if (ignoredEntryId != null && ignoredEntryId.equals(entry.entryId())) {
                continue;
            }
            if (rectOverlap(targetX, targetY, width, height, entry.x(), entry.y(), entry.width(), entry.height())) {
                return false;
            }
        }
        return true;
    }

    public static boolean rectOverlap(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
