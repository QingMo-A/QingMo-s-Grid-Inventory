package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.world.entity.EquipmentSlot;

public record CreativeInsertIntoEquipmentStorageMessage(int tabIndex, int itemIndex, int count, EquipmentSlot equipmentSlot,
                                                        String containerId, int targetX, int targetY,
                                                        boolean rotated, boolean folded) implements GridMessage {
    public static void handle(CreativeInsertIntoEquipmentStorageMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu
                && menu.creativeInsertIntoEquipmentStorage(message.tabIndex(), message.itemIndex(), message.count(), message.equipmentSlot(),
                message.containerId(), message.targetX(), message.targetY(), message.rotated(), message.folded())) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_EQUIPMENT_STORAGE;
    }
}
