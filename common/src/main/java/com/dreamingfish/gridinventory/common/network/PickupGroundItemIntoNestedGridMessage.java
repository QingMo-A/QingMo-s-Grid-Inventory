package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record PickupGroundItemIntoNestedGridMessage(int entityId, NestedContainerPath targetOwnerPath,
                                                    String targetContainerId, int targetX, int targetY,
                                                    boolean rotated) implements GridMessage {
    public static void handle(PickupGroundItemIntoNestedGridMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "PickupGroundItemIntoNestedGridMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.GroundItem(message.entityId());
            GridItemTarget target = message.targetContainerId().isEmpty()
                    ? new GridItemTarget.NestedGridPlacement(message.targetOwnerPath(), message.targetX(),
                    message.targetY(), message.rotated(), false)
                    : new GridItemTarget.NestedEquipmentStoragePlacement(message.targetOwnerPath(),
                    message.targetContainerId(), message.targetX(), message.targetY(), message.rotated(), false);
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(message.rotated(), false))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_NESTED_GRID;
    }
}
