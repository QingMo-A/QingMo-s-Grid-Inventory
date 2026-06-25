package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public record MovePlayerFreeSlotMessage(int sourcePlayerSlot, int targetPlayerSlot, boolean targetFolded) implements GridMessage {

    public static void handle(MovePlayerFreeSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.movePlayerFreeSlot(packet.sourcePlayerSlot(), packet.targetPlayerSlot(), packet.targetFolded());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_PLAYER_FREE_SLOT;
    }
}
