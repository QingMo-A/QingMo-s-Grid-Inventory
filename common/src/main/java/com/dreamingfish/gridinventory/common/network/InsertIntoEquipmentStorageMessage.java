package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.world.entity.EquipmentSlot;

public record InsertIntoEquipmentStorageMessage(int playerSlot, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(InsertIntoEquipmentStorageMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertFromPlayerIntoEquipmentStorage(packet.playerSlot(), packet.equipmentSlot(), packet.containerId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_INTO_EQUIPMENT_STORAGE;
    }
}
