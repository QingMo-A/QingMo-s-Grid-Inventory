package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTransferService;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record CreativeDiscardItemMessage(GridItemSource source) implements GridMessage {
    public static void handle(CreativeDiscardItemMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "CreativeDiscardItemMessage", (player, menu) -> {
            if (GridItemTransferService.discardCreative(menu, message.source())) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_DISCARD_ITEM;
    }
}
