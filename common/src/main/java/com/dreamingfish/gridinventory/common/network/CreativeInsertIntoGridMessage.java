package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.resources.ResourceLocation;

public record CreativeInsertIntoGridMessage(ResourceLocation itemId, int count, int targetX, int targetY,
                                            boolean rotated, boolean folded) implements GridMessage {
    public static void handle(CreativeInsertIntoGridMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu
                && menu.creativeInsertIntoGrid(message.itemId(), message.count(), message.targetX(), message.targetY(),
                message.rotated(), message.folded())) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_GRID;
    }
}
