package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public record MoveSearchableContainerEntryToPlayerSlotMessage(BlockPos blockPos, UUID entryId, int playerSlot,
                                                              int amount) implements GridMessage {
    public static void handle(MoveSearchableContainerEntryToPlayerSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof SearchableGridContainerMenu menu
                && menu.blockPos().equals(packet.blockPos())
                && menu.extractContainerEntryToPlayerSlot(packet.entryId(), packet.playerSlot(), packet.amount())) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
            ModNetworking.syncSearchableContainer(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_SEARCHABLE_CONTAINER_ENTRY_TO_PLAYER_SLOT;
    }
}
