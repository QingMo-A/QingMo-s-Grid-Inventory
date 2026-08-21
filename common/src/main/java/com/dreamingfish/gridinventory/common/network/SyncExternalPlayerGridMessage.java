package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.ExternalPlayerGridSnapshot;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record SyncExternalPlayerGridMessage(GridInventoryData data) implements GridMessage {
    public static void handle(SyncExternalPlayerGridMessage packet, GridMessageContext context) {
        GridInventoryServices.playerData().setPlayerGridInventory(context.player(), packet.data().copy());
        ExternalPlayerGridSnapshot.update(context.player().getUUID(), packet.data());
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_EXTERNAL_PLAYER_GRID;
    }
}
