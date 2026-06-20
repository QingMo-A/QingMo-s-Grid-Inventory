package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import java.util.UUID;

public record TransferNestedGridEntryIntoNestedGridMessage(NestedContainerPath sourceOwnerPath, String sourceContainerId,
                                                           UUID entryId, NestedContainerPath targetOwnerPath,
                                                           String targetContainerId, int targetX, int targetY,
                                                           boolean rotated, boolean targetFolded) implements GridMessage {
    public static void handle(TransferNestedGridEntryIntoNestedGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            if (menu.transferNestedGridEntryIntoNestedGrid(message.sourceOwnerPath(), message.sourceContainerId(), message.entryId(),
                    message.targetOwnerPath(), message.targetContainerId(), message.targetX(), message.targetY(),
                    message.rotated(), message.targetFolded())) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(context.player(), menu);
            }
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_NESTED_GRID_ENTRY_INTO_NESTED_GRID;
    }
}
