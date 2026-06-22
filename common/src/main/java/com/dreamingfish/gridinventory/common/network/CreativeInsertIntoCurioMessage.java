package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.resources.ResourceLocation;

public record CreativeInsertIntoCurioMessage(ResourceLocation itemId, int count, String identifier, int index) implements GridMessage {
    public static void handle(CreativeInsertIntoCurioMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu
                && menu.creativeInsertIntoCurio(message.itemId(), message.count(), message.identifier(), message.index())) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_CURIO;
    }
}
