package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public record ExtractCurioToPlayerSlotMessage(String identifier, int index, int targetPlayerSlot) implements GridMessage {

    public static void handle(ExtractCurioToPlayerSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractCurioToPlayerSlot(packet.identifier(), packet.index(), packet.targetPlayerSlot());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_PLAYER_SLOT;
    }
}
