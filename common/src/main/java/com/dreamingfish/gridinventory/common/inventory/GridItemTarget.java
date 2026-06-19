package com.dreamingfish.gridinventory.common.inventory;

import net.minecraft.world.entity.EquipmentSlot;

public sealed interface GridItemTarget permits
        GridItemTarget.PlayerSlot,
        GridItemTarget.MenuGridPlacement,
        GridItemTarget.EquipmentStoragePlacement,
        GridItemTarget.NestedGridPlacement,
        GridItemTarget.NestedEquipmentStoragePlacement,
        GridItemTarget.AccessorySlot {

    record PlayerSlot(int slot) implements GridItemTarget {
    }

    record MenuGridPlacement(int x, int y, boolean rotated, boolean folded) implements GridItemTarget {
    }

    record EquipmentStoragePlacement(EquipmentSlot slot, String containerId, int x, int y, boolean rotated,
                                     boolean folded) implements GridItemTarget {
    }

    record NestedGridPlacement(NestedContainerPath ownerPath, int x, int y, boolean rotated,
                               boolean folded) implements GridItemTarget {
    }

    record NestedEquipmentStoragePlacement(NestedContainerPath ownerPath, String containerId, int x, int y,
                                           boolean rotated, boolean folded) implements GridItemTarget {
    }

    record AccessorySlot(String identifier, int index) implements GridItemTarget {
    }
}
