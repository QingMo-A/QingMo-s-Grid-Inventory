package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public record InsertFromPlayerInventoryMessage(int playerSlot, int targetX, int targetY, boolean rotated, boolean quick, boolean targetFolded) implements GridMessage {

    public static void handle(InsertFromPlayerInventoryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            if (packet.quick()) {
                menu.quickInsertFromPlayerInventory(packet.playerSlot());
            } else {
                menu.insertFromPlayerInventory(packet.playerSlot(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            }
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_FROM_PLAYER_INVENTORY;
    }
}
