package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record MovePlayerFreeSlotMessage(int sourcePlayerSlot, int targetPlayerSlot, boolean targetFolded) implements GridMessage {

    public static void handle(MovePlayerFreeSlotMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "MovePlayerFreeSlotMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.PlayerSlot(packet.sourcePlayerSlot());
            GridItemTarget target = new GridItemTarget.PlayerSlot(packet.targetPlayerSlot());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(false, packet.targetFolded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_PLAYER_FREE_SLOT;
    }
}
