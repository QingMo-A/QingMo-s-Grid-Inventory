package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

import java.util.UUID;

public record MoveGridEntryMessage(UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(MoveGridEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.moveEntry(packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_GRID_ENTRY;
    }
}
