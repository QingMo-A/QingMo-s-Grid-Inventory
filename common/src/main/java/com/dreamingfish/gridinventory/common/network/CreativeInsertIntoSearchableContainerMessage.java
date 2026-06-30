package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.core.BlockPos;

public record CreativeInsertIntoSearchableContainerMessage(BlockPos blockPos, int tabIndex, int itemIndex,
                                                           int count, int targetX, int targetY, boolean rotated,
                                                           boolean folded) implements GridMessage {
    public static void handle(CreativeInsertIntoSearchableContainerMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof SearchableGridContainerMenu menu
                && menu.blockPos().equals(packet.blockPos())
                && menu.creativeInsertIntoContainerGrid(packet.tabIndex(), packet.itemIndex(), packet.count(),
                packet.targetX(), packet.targetY(), packet.rotated(), packet.folded())) {
            menu.broadcastChanges();
            ModNetworking.syncSearchableContainer(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_SEARCHABLE_CONTAINER;
    }
}
