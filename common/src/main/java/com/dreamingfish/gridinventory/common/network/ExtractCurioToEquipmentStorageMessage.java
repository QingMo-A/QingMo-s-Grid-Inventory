package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.world.entity.EquipmentSlot;

public record ExtractCurioToEquipmentStorageMessage(String identifier, int index, EquipmentSlot equipmentSlot,
                                                   String containerId, int targetX, int targetY,
                                                   boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(ExtractCurioToEquipmentStorageMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertCurioIntoEquipmentStorage(packet.identifier(), packet.index(), packet.equipmentSlot(), packet.containerId(),
                    packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_EQUIPMENT_STORAGE;
    }
}
