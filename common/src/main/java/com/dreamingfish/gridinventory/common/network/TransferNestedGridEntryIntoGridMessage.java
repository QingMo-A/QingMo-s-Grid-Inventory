package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import java.util.UUID;

public record TransferNestedGridEntryIntoGridMessage(NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId,
                                                     int targetX, int targetY, boolean rotated,
                                                     boolean targetFolded) implements GridMessage {
    public static void handle(TransferNestedGridEntryIntoGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferNestedGridEntryIntoGrid(message.sourceOwnerPath(), message.sourceContainerId(), message.entryId(),
                    message.targetX(), message.targetY(), message.rotated(), message.targetFolded());
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_NESTED_GRID_ENTRY_INTO_GRID;
    }
}
