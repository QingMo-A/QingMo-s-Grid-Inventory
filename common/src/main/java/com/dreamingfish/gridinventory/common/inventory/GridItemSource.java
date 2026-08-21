package com.dreamingfish.gridinventory.common.inventory;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public sealed interface GridItemSource permits
        GridItemSource.PlayerSlot,
        GridItemSource.MenuGridEntry,
        GridItemSource.EquipmentStorageEntry,
        GridItemSource.NestedGridEntry,
        GridItemSource.NestedEquipmentStorageEntry,
        GridItemSource.AccessorySlot,
        GridItemSource.GroundItem,
        GridItemSource.CreativeItem,
        GridItemSource.PlayerGridEntry,
        GridItemSource.MenuCarried {

    record PlayerSlot(int slot) implements GridItemSource {
    }

    record MenuGridEntry(UUID entryId) implements GridItemSource {
    }

    record EquipmentStorageEntry(EquipmentSlot slot, String containerId, UUID entryId) implements GridItemSource {
    }

    record NestedGridEntry(NestedContainerPath ownerPath, UUID entryId) implements GridItemSource {
    }

    record NestedEquipmentStorageEntry(NestedContainerPath ownerPath, String containerId, UUID entryId) implements GridItemSource {
    }

    record AccessorySlot(String identifier, int index) implements GridItemSource {
    }

    record GroundItem(int entityId) implements GridItemSource {
    }

    record CreativeItem(int tabIndex, int itemIndex, int count) implements GridItemSource {
    }

    record PlayerGridEntry(UUID entryId) implements GridItemSource {
    }

    record MenuCarried(int containerId) implements GridItemSource {
    }
}
