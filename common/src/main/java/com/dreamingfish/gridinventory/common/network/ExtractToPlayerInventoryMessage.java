package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

import java.util.UUID;

public record ExtractToPlayerInventoryMessage(UUID entryId, int amount) implements GridMessage {

    public static void handle(ExtractToPlayerInventoryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractToPlayerInventory(packet.entryId(), packet.amount());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_TO_PLAYER_INVENTORY;
    }
}
