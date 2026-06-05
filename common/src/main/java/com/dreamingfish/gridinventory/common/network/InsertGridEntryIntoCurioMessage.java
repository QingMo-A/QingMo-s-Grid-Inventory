package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record InsertGridEntryIntoCurioMessage(UUID entryId, String identifier, int index) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
    }

    public static InsertGridEntryIntoCurioMessage decode(FriendlyByteBuf buf) {
        return new InsertGridEntryIntoCurioMessage(buf.readUUID(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(InsertGridEntryIntoCurioMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertGridEntryIntoCurio(packet.entryId(), packet.identifier(), packet.index());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_GRID_ENTRY_INTO_CURIO;
    }
}
