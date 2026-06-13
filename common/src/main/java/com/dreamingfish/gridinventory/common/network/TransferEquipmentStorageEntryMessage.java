package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record TransferEquipmentStorageEntryMessage(EquipmentSlot sourceSlot, String sourceContainerId, UUID entryId,
                                                  EquipmentSlot targetSlot, String targetContainerId,
                                                  int targetX, int targetY, boolean rotated,
                                                  boolean targetFolded) implements GridMessage {

    public static void handle(TransferEquipmentStorageEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferEquipmentEntryBetweenStorages(packet.sourceSlot(), packet.sourceContainerId(), packet.entryId(),
                    packet.targetSlot(), packet.targetContainerId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY;
    }
}
