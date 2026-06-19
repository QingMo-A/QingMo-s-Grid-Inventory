package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record InsertPlayerSlotIntoNestedGridMessage(int playerSlot, NestedContainerPath targetOwnerPath,
                                                    String targetContainerId, int targetX, int targetY,
                                                    boolean rotated, boolean targetFolded) implements GridMessage {
    public static void handle(InsertPlayerSlotIntoNestedGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertPlayerSlotIntoNestedGrid(message.playerSlot(), message.targetOwnerPath(),
                    message.targetContainerId(), message.targetX(), message.targetY(), message.rotated(),
                    message.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_PLAYER_SLOT_INTO_NESTED_GRID;
    }
}
