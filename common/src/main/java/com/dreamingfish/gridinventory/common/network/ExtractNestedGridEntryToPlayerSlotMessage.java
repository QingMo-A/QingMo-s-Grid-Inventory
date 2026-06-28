package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import java.util.UUID;

public record ExtractNestedGridEntryToPlayerSlotMessage(NestedContainerPath sourceOwnerPath, String sourceContainerId,
                                                        UUID entryId, int playerSlot, int amount) implements GridMessage {
    public static void handle(ExtractNestedGridEntryToPlayerSlotMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "ExtractNestedGridEntryToPlayerSlotMessage", (player, menu) -> {
            GridItemSource source = message.sourceContainerId().isEmpty()
                    ? new GridItemSource.NestedGridEntry(message.sourceOwnerPath(), message.entryId())
                    : new GridItemSource.NestedEquipmentStorageEntry(message.sourceOwnerPath(),
                    message.sourceContainerId(), message.entryId());
            GridItemTarget target = new GridItemTarget.PlayerSlot(message.playerSlot());
            GridMoveOptions options = new GridMoveOptions(message.amount(), false, false);
            if (GridItemMoveService.move(menu, source, target, options)) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_NESTED_GRID_ENTRY_TO_PLAYER_SLOT;
    }
}
