package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record InsertEquipmentStorageEntryIntoCurioMessage(EquipmentSlot sourceSlot, String containerId, UUID entryId,
                                                         String identifier, int index) implements GridMessage {

    public static void handle(InsertEquipmentStorageEntryIntoCurioMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertEquipmentStorageEntryIntoCurio(packet.sourceSlot(), packet.containerId(), packet.entryId(), packet.identifier(), packet.index());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_EQUIPMENT_STORAGE_ENTRY_INTO_CURIO;
    }
}
