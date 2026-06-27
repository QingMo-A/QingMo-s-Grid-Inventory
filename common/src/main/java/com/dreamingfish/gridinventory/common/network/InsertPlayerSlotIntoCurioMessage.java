package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record InsertPlayerSlotIntoCurioMessage(int sourcePlayerSlot, String identifier, int index) implements GridMessage {

    public static void handle(InsertPlayerSlotIntoCurioMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "InsertPlayerSlotIntoCurioMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.PlayerSlot(packet.sourcePlayerSlot());
            GridItemTarget target = new GridItemTarget.AccessorySlot(packet.identifier(), packet.index());
            if (GridItemMoveService.move(menu, source, target, GridMoveOptions.all(false, false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_PLAYER_SLOT_INTO_CURIO;
    }
}
