package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.resources.ResourceLocation;

public record CreativeInsertIntoPlayerSlotMessage(ResourceLocation itemId, int count, int playerSlot) implements GridMessage {
    public static void handle(CreativeInsertIntoPlayerSlotMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu
                && menu.creativeInsertIntoPlayerSlot(message.itemId(), message.count(), message.playerSlot())) {
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_PLAYER_SLOT;
    }
}
