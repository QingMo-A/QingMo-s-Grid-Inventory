package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record TransferEquipmentStorageEntryIntoNestedGridMessage(EquipmentSlot sourceSlot, String sourceContainerId,
                                                                 UUID entryId, NestedContainerPath targetOwnerPath,
                                                                 String targetContainerId, int targetX, int targetY,
                                                                 boolean rotated, boolean targetFolded) implements GridMessage {
    public static void handle(TransferEquipmentStorageEntryIntoNestedGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferEquipmentEntryIntoNestedGrid(message.sourceSlot(), message.sourceContainerId(), message.entryId(),
                    message.targetOwnerPath(), message.targetContainerId(), message.targetX(), message.targetY(),
                    message.rotated(), message.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY_INTO_NESTED_GRID;
    }
}
