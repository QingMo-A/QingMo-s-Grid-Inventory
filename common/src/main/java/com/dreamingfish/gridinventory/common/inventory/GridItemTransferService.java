package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class GridItemTransferService {
    private GridItemTransferService() {
    }

    public static ItemStack prepareForTarget(ItemStack stack, GridItemTarget target) {
        return GridItemTransferRules.prepareForTarget(stack, target);
    }

    public static int targetDepth(GridItemTarget target, int menuGridDepth) {
        return GridItemTransferRules.targetDepth(target, menuGridDepth);
    }

    public static boolean canPlace(GridInventoryData inventory, ItemStack stack, GridItemTarget target,
                                   UUID ignoredEntryId, int menuGridDepth) {
        if (target instanceof GridItemTarget.MenuGridPlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        if (target instanceof GridItemTarget.NestedGridPlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        return false;
    }

    public static void add(GridInventoryData inventory, ItemStack stack, GridItemTarget target) {
        if (target instanceof GridItemTarget.MenuGridPlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        } else if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        } else if (target instanceof GridItemTarget.NestedGridPlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        } else if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        }
    }
}
