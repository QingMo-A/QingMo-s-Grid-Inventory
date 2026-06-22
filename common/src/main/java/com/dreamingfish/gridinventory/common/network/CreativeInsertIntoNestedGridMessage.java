package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record CreativeInsertIntoNestedGridMessage(int tabIndex, int itemIndex, int count, NestedContainerPath targetOwnerPath,
                                                  String targetContainerId, int targetX, int targetY,
                                                  boolean rotated, boolean folded) implements GridMessage {
    public static void handle(CreativeInsertIntoNestedGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu
                && menu.creativeInsertIntoNestedGrid(message.tabIndex(), message.itemIndex(), message.count(), message.targetOwnerPath(),
                message.targetContainerId(), message.targetX(), message.targetY(), message.rotated(), message.folded())) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_NESTED_GRID;
    }
}
