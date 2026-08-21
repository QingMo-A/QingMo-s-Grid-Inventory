package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.item.ItemStack;

public final class GridItemTransferRules {
    private GridItemTransferRules() {
    }

    public static ItemStack prepareForTarget(ItemStack stack, GridItemTarget target) {
        ItemStack prepared = stack.copy();
        if (!(prepared.getItem() instanceof GridBackpackItem)) {
            return prepared;
        }
        Boolean folded = targetFolded(target);
        if (folded != null) {
            GridInventoryServices.itemStackData().setBackpackFolded(prepared, folded);
        }
        return prepared;
    }

    public static int targetDepth(GridItemTarget target, int menuGridDepth) {
        if (target instanceof GridItemTarget.MenuGridPlacement) {
            return menuGridDepth;
        }
        if (target instanceof GridItemTarget.EquipmentStoragePlacement) {
            return 1;
        }
        if (target instanceof GridItemTarget.NestedGridPlacement nested) {
            return nested.ownerPath().depth() + 1;
        }
        if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement nested) {
            return nested.ownerPath().depth() + 1;
        }
        if (target instanceof GridItemTarget.PlayerGridPlacement) {
            return 0;
        }
        return 0;
    }

    private static Boolean targetFolded(GridItemTarget target) {
        if (target instanceof GridItemTarget.MenuGridPlacement placement) {
            return placement.folded();
        }
        if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            return placement.folded();
        }
        if (target instanceof GridItemTarget.NestedGridPlacement placement) {
            return placement.folded();
        }
        if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            return placement.folded();
        }
        if (target instanceof GridItemTarget.PlayerGridPlacement placement) {
            return placement.folded();
        }
        return null;
    }
}
