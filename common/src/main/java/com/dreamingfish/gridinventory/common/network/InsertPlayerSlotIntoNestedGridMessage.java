package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record InsertPlayerSlotIntoNestedGridMessage(int playerSlot, NestedContainerPath targetOwnerPath,
                                                    String targetContainerId, int targetX, int targetY,
                                                    boolean rotated, boolean targetFolded) implements GridMessage {
    public static void handle(InsertPlayerSlotIntoNestedGridMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "InsertPlayerSlotIntoNestedGridMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.PlayerSlot(message.playerSlot());
            GridItemTarget target = message.targetContainerId().isEmpty()
                    ? new GridItemTarget.NestedGridPlacement(message.targetOwnerPath(), message.targetX(),
                    message.targetY(), message.rotated(), message.targetFolded())
                    : new GridItemTarget.NestedEquipmentStoragePlacement(message.targetOwnerPath(),
                    message.targetContainerId(), message.targetX(), message.targetY(), message.rotated(),
                    message.targetFolded());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(message.rotated(), message.targetFolded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_PLAYER_SLOT_INTO_NESTED_GRID;
    }
}
