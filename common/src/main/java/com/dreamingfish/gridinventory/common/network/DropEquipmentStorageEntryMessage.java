package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record DropEquipmentStorageEntryMessage(EquipmentSlot equipmentSlot, String containerId, UUID entryId) implements GridMessage {

    public static void handle(DropEquipmentStorageEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.dropEquipmentStorageEntry(packet.equipmentSlot(), packet.containerId(), packet.entryId());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.DROP_EQUIPMENT_STORAGE_ENTRY;
    }
}
