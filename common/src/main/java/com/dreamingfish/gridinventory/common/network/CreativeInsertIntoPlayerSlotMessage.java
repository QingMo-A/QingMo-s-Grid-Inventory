package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record CreativeInsertIntoPlayerSlotMessage(int tabIndex, int itemIndex, int count, int playerSlot) implements GridMessage {
    public static void handle(CreativeInsertIntoPlayerSlotMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "CreativeInsertIntoPlayerSlotMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.CreativeItem(
                    message.tabIndex(), message.itemIndex(), message.count());
            GridItemTarget target = new GridItemTarget.PlayerSlot(message.playerSlot());
            if (GridItemMoveService.move(menu, source, target, GridMoveOptions.all(false, false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_PLAYER_SLOT;
    }
}
