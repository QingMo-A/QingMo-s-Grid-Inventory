package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record ExtractCurioToPlayerSlotMessage(String identifier, int index, int targetPlayerSlot) implements GridMessage {

    public static void handle(ExtractCurioToPlayerSlotMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "ExtractCurioToPlayerSlotMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.AccessorySlot(packet.identifier(), packet.index());
            GridItemTarget target = new GridItemTarget.PlayerSlot(packet.targetPlayerSlot());
            if (GridItemMoveService.move(menu, source, target, GridMoveOptions.all(false, false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_PLAYER_SLOT;
    }
}
