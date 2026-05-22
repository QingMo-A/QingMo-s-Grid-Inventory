package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.api.IGridInventory;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class GridAutoInsertHelper {
    private GridAutoInsertHelper() {
    }

    public record Placement(int x, int y, boolean rotated) {
    }

    public static Optional<Placement> findFirstPlacement(IGridInventory inventory, ItemStack stack) {
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (GridPlacementValidator.canPlace(inventory, stack, x, y, false, null)) {
                    return Optional.of(new Placement(x, y, false));
                }
            }
        }
        if (GridItemSizeManager.getSize(stack).rotatable()) {
            for (int y = 0; y < inventory.getRows(); y++) {
                for (int x = 0; x < inventory.getColumns(); x++) {
                    if (GridPlacementValidator.canPlace(inventory, stack, x, y, true, null)) {
                        return Optional.of(new Placement(x, y, true));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
