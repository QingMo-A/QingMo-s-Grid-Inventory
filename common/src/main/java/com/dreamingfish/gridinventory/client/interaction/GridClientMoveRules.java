package com.dreamingfish.gridinventory.client.interaction;

import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;

/** Client-side mirror of the currently supported unified movement routes. */
public final class GridClientMoveRules {
    private GridClientMoveRules() {
    }

    public static boolean supports(GridItemSource source, GridItemTarget target) {
        if (source instanceof GridItemSource.MenuCarried) {
            return isExternalSidebarTarget(target);
        }
        if (target instanceof GridItemTarget.MenuSlot) {
            return isExternalSidebarSource(source);
        }
        if (target instanceof GridItemTarget.PlayerGridPlacement) {
            return isExternalSidebarSource(source);
        }
        if (source instanceof GridItemSource.PlayerGridEntry) {
            return isExternalSidebarTarget(target);
        }
        if (source instanceof GridItemSource.GroundItem || source instanceof GridItemSource.CreativeItem) {
            return isMainPlacementTarget(target);
        }
        if (source instanceof GridItemSource.MenuGridEntry
                || source instanceof GridItemSource.EquipmentStorageEntry) {
            return target instanceof GridItemTarget.MenuGridPlacement
                    || isNestedPlacement(target)
                    || target instanceof GridItemTarget.EquipmentStoragePlacement
                    || target instanceof GridItemTarget.PlayerSlot
                    || target instanceof GridItemTarget.AccessorySlot;
        }
        if (source instanceof GridItemSource.NestedGridEntry
                || source instanceof GridItemSource.NestedEquipmentStorageEntry) {
            return target instanceof GridItemTarget.MenuGridPlacement
                    || isNestedPlacement(target)
                    || target instanceof GridItemTarget.EquipmentStoragePlacement
                    || target instanceof GridItemTarget.PlayerSlot;
        }
        if (source instanceof GridItemSource.PlayerSlot) {
            return isMainPlacementTarget(target) || target instanceof GridItemTarget.PlayerGridPlacement
                    || target instanceof GridItemTarget.MenuSlot;
        }
        if (source instanceof GridItemSource.AccessorySlot) {
            return target instanceof GridItemTarget.MenuGridPlacement
                    || isNestedPlacement(target)
                    || target instanceof GridItemTarget.EquipmentStoragePlacement
                    || target instanceof GridItemTarget.PlayerSlot
                    || target instanceof GridItemTarget.PlayerGridPlacement
                    || target instanceof GridItemTarget.AccessorySlot
                    || target instanceof GridItemTarget.MenuSlot;
        }
        return false;
    }

    private static boolean isMainPlacementTarget(GridItemTarget target) {
        return target instanceof GridItemTarget.MenuGridPlacement
                || isNestedPlacement(target)
                || target instanceof GridItemTarget.EquipmentStoragePlacement
                || target instanceof GridItemTarget.PlayerSlot
                || target instanceof GridItemTarget.AccessorySlot;
    }

    private static boolean isNestedPlacement(GridItemTarget target) {
        return target instanceof GridItemTarget.NestedGridPlacement
                || target instanceof GridItemTarget.NestedEquipmentStoragePlacement;
    }

    private static boolean isExternalSidebarSource(GridItemSource source) {
        return source instanceof GridItemSource.PlayerGridEntry
                || source instanceof GridItemSource.EquipmentStorageEntry
                || source instanceof GridItemSource.PlayerSlot
                || source instanceof GridItemSource.AccessorySlot;
    }

    private static boolean isExternalSidebarTarget(GridItemTarget target) {
        return target instanceof GridItemTarget.PlayerGridPlacement
                || target instanceof GridItemTarget.EquipmentStoragePlacement
                || target instanceof GridItemTarget.PlayerSlot
                || target instanceof GridItemTarget.AccessorySlot;
    }
}
