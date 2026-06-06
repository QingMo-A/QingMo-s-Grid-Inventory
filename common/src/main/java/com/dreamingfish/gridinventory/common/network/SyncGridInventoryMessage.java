package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public record SyncGridInventoryMessage(GridInventoryData data) implements GridMessage {

    public static void handle(SyncGridInventoryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.replaceGridData(packet.data());
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_GRID_INVENTORY;
    }
}
