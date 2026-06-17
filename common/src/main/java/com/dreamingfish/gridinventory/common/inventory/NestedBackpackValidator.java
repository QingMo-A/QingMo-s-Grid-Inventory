package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.item.ItemStack;

public final class NestedBackpackValidator {
    private NestedBackpackValidator() {
    }

    public static boolean canPlaceInTargetDepth(int targetDepth, ItemStack stack) {
        int stackDepth = nestedContainerDepth(stack);
        return stackDepth == 0 || Math.max(0, targetDepth) + stackDepth <= maxDepth();
    }

    public static int nestedContainerDepth(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        GridInventoryData grid = GridInventoryServices.itemStackData().getGridInventory(stack);
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        boolean container = stack.getItem() instanceof GridBackpackItem
                || grid != null
                || storage != null && !storage.containers().isEmpty();
        if (!container) {
            return 0;
        }
        int childDepth = 0;
        if (grid != null) {
            childDepth = Math.max(childDepth, grid.getEntries().stream()
                    .mapToInt(entry -> nestedContainerDepth(entry.stack()))
                    .max().orElse(0));
        }
        if (storage != null) {
            childDepth = Math.max(childDepth, storage.containers().stream()
                    .flatMap(containerData -> containerData.inventory().getEntries().stream())
                    .mapToInt(entry -> nestedContainerDepth(entry.stack()))
                    .max().orElse(0));
        }
        return 1 + childDepth;
    }

    private static int maxDepth() {
        return Math.max(1, GridInventoryServices.config().maxNestedBackpackDepth());
    }
}
