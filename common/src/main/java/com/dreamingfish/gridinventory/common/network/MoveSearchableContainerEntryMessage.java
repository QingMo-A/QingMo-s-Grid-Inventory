package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public record MoveSearchableContainerEntryMessage(BlockPos blockPos, UUID entryId, int targetX, int targetY,
                                                  boolean rotated, boolean folded) implements GridMessage {
    public static void handle(MoveSearchableContainerEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof SearchableGridContainerMenu menu
                && menu.blockPos().equals(packet.blockPos())
                && menu.moveContainerEntry(packet.entryId(), packet.targetX(), packet.targetY(),
                packet.rotated(), packet.folded())) {
            menu.broadcastChanges();
            ModNetworking.syncSearchableContainer(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_SEARCHABLE_CONTAINER_ENTRY;
    }
}
