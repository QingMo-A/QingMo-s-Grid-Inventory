package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record CreativeInsertIntoNestedGridMessage(int tabIndex, int itemIndex, int count, NestedContainerPath targetOwnerPath,
                                                  String targetContainerId, int targetX, int targetY,
                                                  boolean rotated, boolean folded) implements GridMessage {
    public static void handle(CreativeInsertIntoNestedGridMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "CreativeInsertIntoNestedGridMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.CreativeItem(message.tabIndex(), message.itemIndex(),
                    message.count());
            GridItemTarget target = message.targetContainerId().isEmpty()
                    ? new GridItemTarget.NestedGridPlacement(message.targetOwnerPath(), message.targetX(),
                    message.targetY(), message.rotated(), message.folded())
                    : new GridItemTarget.NestedEquipmentStoragePlacement(message.targetOwnerPath(),
                    message.targetContainerId(), message.targetX(), message.targetY(), message.rotated(),
                    message.folded());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(message.rotated(), message.folded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_NESTED_GRID;
    }
}
