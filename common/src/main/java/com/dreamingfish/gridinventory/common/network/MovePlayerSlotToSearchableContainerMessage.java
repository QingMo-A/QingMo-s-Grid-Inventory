package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.core.BlockPos;

public record MovePlayerSlotToSearchableContainerMessage(BlockPos blockPos, int playerSlot, int targetX, int targetY,
                                                         boolean rotated, boolean folded) implements GridMessage {
    public static void handle(MovePlayerSlotToSearchableContainerMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof SearchableGridContainerMenu menu
                && menu.blockPos().equals(packet.blockPos())
                && menu.insertPlayerSlotIntoContainerGrid(packet.playerSlot(), packet.targetX(), packet.targetY(),
                packet.rotated(), packet.folded())) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
            ModNetworking.syncSearchableContainer(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_PLAYER_SLOT_TO_SEARCHABLE_CONTAINER;
    }
}
