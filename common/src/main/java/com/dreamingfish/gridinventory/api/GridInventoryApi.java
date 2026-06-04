package com.dreamingfish.gridinventory.api;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.GridStackMerger;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class GridInventoryApi {
    private GridInventoryApi() {
    }

    public static GridItemSize getItemSize(ItemStack stack) {
        return GridItemSizeManager.getSize(stack);
    }

    public static boolean canPlace(IGridInventory inventory, ItemStack stack, int x, int y, boolean rotated) {
        return GridPlacementValidator.canPlace(inventory, stack, x, y, rotated, null);
    }

    public static ItemStack insert(IGridInventory inventory, ItemStack stack, GridInsertMode mode) {
        return inventory.insert(stack, mode);
    }

    public static ItemStack insertAt(IGridInventory inventory, ItemStack stack, int x, int y, boolean rotated, GridInsertMode mode) {
        if (!GridPlacementValidator.canPlace(inventory, stack, x, y, rotated, null)) {
            return stack;
        }
        if (mode == GridInsertMode.EXECUTE && inventory instanceof com.dreamingfish.gridinventory.common.data.GridInventoryData data) {
            ItemStack remainder = GridStackMerger.mergeIntoExisting(data, stack);
            if (!remainder.isEmpty()) {
                data.add(remainder.copy(), x, y, rotated);
            }
            inventory.setChanged();
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    public static Optional<GridEntry> getEntryAt(IGridInventory inventory, int x, int y) {
        return inventory.getEntries().stream().filter(entry -> entry.contains(x, y)).findFirst();
    }
}
