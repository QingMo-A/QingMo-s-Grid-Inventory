package com.dreamingfish.gridinventory.common.inventory;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public sealed interface GridItemSource permits
        GridItemSource.PlayerSlot,
        GridItemSource.MenuGridEntry,
        GridItemSource.EquipmentStorageEntry,
        GridItemSource.NestedGridEntry,
        GridItemSource.NestedEquipmentStorageEntry,
        GridItemSource.AccessorySlot {

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
}
