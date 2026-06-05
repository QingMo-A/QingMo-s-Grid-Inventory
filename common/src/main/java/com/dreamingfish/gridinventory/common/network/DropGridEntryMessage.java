package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record DropGridEntryMessage(UUID entryId) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
    }

    public static DropGridEntryMessage decode(FriendlyByteBuf buf) {
        return new DropGridEntryMessage(buf.readUUID());
    }

    public static void handle(DropGridEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.dropGridEntry(packet.entryId());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.DROP_GRID_ENTRY;
    }
}
