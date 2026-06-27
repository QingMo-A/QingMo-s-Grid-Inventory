package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record ExtractCurioToGridMessage(String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(ExtractCurioToGridMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "ExtractCurioToGridMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.AccessorySlot(packet.identifier(), packet.index());
            GridItemTarget target = new GridItemTarget.MenuGridPlacement(packet.targetX(), packet.targetY(),
                    packet.rotated(), packet.targetFolded());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(packet.rotated(), packet.targetFolded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_GRID;
    }
}
