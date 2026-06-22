package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record CreativeInsertIntoPlayerSlotMessage(int tabIndex, int itemIndex, int count, int playerSlot) implements GridMessage {
    public static void handle(CreativeInsertIntoPlayerSlotMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu
                && menu.creativeInsertIntoPlayerSlot(message.tabIndex(), message.itemIndex(), message.count(), message.playerSlot())) {
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_PLAYER_SLOT;
    }
}
