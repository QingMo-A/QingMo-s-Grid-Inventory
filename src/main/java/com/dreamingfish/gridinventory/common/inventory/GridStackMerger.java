package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.item.ItemStack;

public final class GridStackMerger {
    private GridStackMerger() {
    }

    public static ItemStack mergeIntoExisting(GridInventoryData inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        for (var entry : inventory.getEntries()) {
            ItemStack existing = entry.stack();
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remainder) && existing.getCount() < existing.getMaxStackSize()) {
                int moved = Math.min(remainder.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(moved);
                remainder.shrink(moved);
                inventory.setChanged();
                if (remainder.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return remainder;
    }

    public static boolean canFullyMergeIntoExisting(GridInventoryData inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int remaining = stack.getCount();
        for (var entry : inventory.getEntries()) {
            ItemStack existing = entry.stack();
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                remaining -= existing.getMaxStackSize() - existing.getCount();
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
