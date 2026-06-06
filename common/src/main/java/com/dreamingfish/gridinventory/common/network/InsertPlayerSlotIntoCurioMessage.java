package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public record InsertPlayerSlotIntoCurioMessage(int sourcePlayerSlot, String identifier, int index) implements GridMessage {

    public static void handle(InsertPlayerSlotIntoCurioMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertPlayerSlotIntoCurio(packet.sourcePlayerSlot(), packet.identifier(), packet.index());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_PLAYER_SLOT_INTO_CURIO;
    }
}
