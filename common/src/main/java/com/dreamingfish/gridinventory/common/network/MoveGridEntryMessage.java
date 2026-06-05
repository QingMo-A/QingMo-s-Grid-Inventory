package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record MoveGridEntryMessage(UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    public static MoveGridEntryMessage decode(FriendlyByteBuf buf) {
        return new MoveGridEntryMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(MoveGridEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.moveEntry(packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_GRID_ENTRY;
    }
}
