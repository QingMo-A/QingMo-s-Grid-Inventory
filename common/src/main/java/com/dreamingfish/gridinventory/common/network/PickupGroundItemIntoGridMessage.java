package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record PickupGroundItemIntoGridMessage(int entityId, int targetX, int targetY, boolean rotated) implements GridMessage {

    public static void handle(PickupGroundItemIntoGridMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "PickupGroundItemIntoGridMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.GroundItem(packet.entityId());
            GridItemTarget target = new GridItemTarget.MenuGridPlacement(packet.targetX(), packet.targetY(),
                    packet.rotated(), false);
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(packet.rotated(), false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_GRID;
    }
}
