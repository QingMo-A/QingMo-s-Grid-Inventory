package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.core.BlockPos;

public record SyncSearchableContainerGridMessage(BlockPos blockPos, GridInventoryData data) implements GridMessage {
    public static void handle(SyncSearchableContainerGridMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof SearchableGridContainerMenu menu
                && menu.blockPos().equals(packet.blockPos())) {
            menu.replaceContainerGridData(packet.data());
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_SEARCHABLE_CONTAINER_GRID;
    }
}
