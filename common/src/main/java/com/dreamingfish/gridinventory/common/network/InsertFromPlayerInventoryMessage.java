package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record InsertFromPlayerInventoryMessage(int playerSlot, int targetX, int targetY, boolean rotated, boolean quick, boolean targetFolded) implements GridMessage {

    public static void handle(InsertFromPlayerInventoryMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "InsertFromPlayerInventoryMessage", (player, menu) -> {
            if (packet.quick()) {
                menu.quickInsertFromPlayerInventory(packet.playerSlot());
                ModNetworking.syncMenu(player, menu);
            } else {
                GridItemSource source = new GridItemSource.PlayerSlot(packet.playerSlot());
                GridItemTarget target = new GridItemTarget.MenuGridPlacement(packet.targetX(), packet.targetY(),
                        packet.rotated(), packet.targetFolded());
                if (GridItemMoveService.move(menu, source, target,
                        GridMoveOptions.all(packet.rotated(), packet.targetFolded()))) {
                    menu.broadcastChanges();
                    ModNetworking.syncMenu(player, menu);
                }
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_FROM_PLAYER_INVENTORY;
    }
}
