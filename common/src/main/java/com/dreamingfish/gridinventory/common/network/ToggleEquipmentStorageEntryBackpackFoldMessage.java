package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record ToggleEquipmentStorageEntryBackpackFoldMessage(EquipmentSlot slot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated) implements GridMessage {

    public static void handle(ToggleEquipmentStorageEntryBackpackFoldMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.toggleEquipmentStorageEntryBackpackFold(packet.slot(), packet.containerId(), packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated());
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TOGGLE_EQUIPMENT_STORAGE_ENTRY_BACKPACK_FOLD;
    }
}
