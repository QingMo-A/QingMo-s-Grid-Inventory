package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import java.util.UUID;

public record InsertGridEntryIntoCurioMessage(UUID entryId, String identifier, int index) implements GridMessage {

    public static void handle(InsertGridEntryIntoCurioMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "InsertGridEntryIntoCurioMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.MenuGridEntry(packet.entryId());
            GridItemTarget target = new GridItemTarget.AccessorySlot(packet.identifier(), packet.index());
            if (GridItemMoveService.move(menu, source, target, GridMoveOptions.all(false, false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_GRID_ENTRY_INTO_CURIO;
    }
}
