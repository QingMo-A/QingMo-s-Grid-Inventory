package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record PickupGroundItemIntoCurioMessage(int entityId, String identifier, int index) implements GridMessage {
    public static void handle(PickupGroundItemIntoCurioMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "PickupGroundItemIntoCurioMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.GroundItem(message.entityId());
            GridItemTarget target = new GridItemTarget.AccessorySlot(message.identifier(), message.index());
            if (GridItemMoveService.move(menu, source, target, GridMoveOptions.all(false, false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_CURIO;
    }
}
