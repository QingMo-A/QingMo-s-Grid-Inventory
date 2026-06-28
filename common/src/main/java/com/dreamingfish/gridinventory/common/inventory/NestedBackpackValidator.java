package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class NestedBackpackValidator {
    private NestedBackpackValidator() {
    }

    public static boolean canPlaceInTargetDepth(int targetDepth, ItemStack stack) {
        int stackDepth = nestedBackpackDepth(stack);
        boolean result = stackDepth == 0 || Math.max(0, targetDepth) + stackDepth <= maxDepth();
        debugDepthCheck(targetDepth, stack, stackDepth, result);
        return result;
    }

    public static int nestedBackpackDepth(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof GridBackpackItem && GridBackpackItem.isFolded(stack)) {
            return 0;
        }
        int childDepth = maxBackpackDepthInside(stack);
        if (stack.getItem() instanceof GridBackpackItem) {
            return 1 + childDepth;
        }
        return childDepth;
    }

    @Deprecated
    public static int nestedContainerDepth(ItemStack stack) {
        return nestedBackpackDepth(stack);
    }

    private static int maxBackpackDepthInside(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        GridInventoryData grid = GridInventoryServices.itemStackData().getGridInventory(stack);
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        int childDepth = 0;
        if (grid != null) {
            childDepth = Math.max(childDepth, grid.getEntries().stream()
                    .mapToInt(entry -> nestedBackpackDepth(entry.stack()))
                    .max().orElse(0));
        }
        if (storage != null) {
            childDepth = Math.max(childDepth, storage.containers().stream()
                    .flatMap(containerData -> containerData.inventory().getEntries().stream())
                    .mapToInt(entry -> nestedBackpackDepth(entry.stack()))
                    .max().orElse(0));
        }
        return childDepth;
    }

    private static int maxDepth() {
        return Math.max(1, GridInventoryServices.config().maxNestedBackpackDepth());
    }

    private static void debugDepthCheck(int targetDepth, ItemStack stack, int stackDepth, boolean result) {
        if (!DFGridInventory.LOGGER.isDebugEnabled()) {
            return;
        }
        GridInventoryData grid = GridInventoryServices.itemStackData().getGridInventory(stack);
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        DFGridInventory.LOGGER.debug(
                "Nested backpack depth check item={} isGridBackpackItem={} hasGridInventory={} hasEquipmentStorage={} nestedBackpackDepth={} targetBackpackDepth={} maxNestedBackpackDepth={} canPlaceInTargetDepth={}",
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                stack.getItem() instanceof GridBackpackItem,
                grid != null,
                storage != null && !storage.containers().isEmpty(),
                stackDepth,
                Math.max(0, targetDepth),
                maxDepth(),
                result);
    }
}
