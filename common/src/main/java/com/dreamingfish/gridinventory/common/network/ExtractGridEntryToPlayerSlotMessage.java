package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

import java.util.UUID;

public record ExtractGridEntryToPlayerSlotMessage(UUID entryId, int playerSlot, int amount) implements GridMessage {

    public static void handle(ExtractGridEntryToPlayerSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractToPlayerSlot(packet.entryId(), packet.playerSlot(), packet.amount());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_GRID_ENTRY_TO_PLAYER_SLOT;
    }
}
