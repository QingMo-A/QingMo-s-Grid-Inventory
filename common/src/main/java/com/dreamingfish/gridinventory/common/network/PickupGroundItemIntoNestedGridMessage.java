package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record PickupGroundItemIntoNestedGridMessage(int entityId, NestedContainerPath targetOwnerPath,
                                                    String targetContainerId, int targetX, int targetY,
                                                    boolean rotated) implements GridMessage {
    public static void handle(PickupGroundItemIntoNestedGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            if (menu.pickupGroundItemIntoNestedGrid(message.entityId(), message.targetOwnerPath(),
                    message.targetContainerId(), message.targetX(), message.targetY(), message.rotated())) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(context.player(), menu);
            }
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_NESTED_GRID;
    }
}
