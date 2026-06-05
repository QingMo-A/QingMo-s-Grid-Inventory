package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record ExtractCurioToGridMessage(String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    public static ExtractCurioToGridMessage decode(FriendlyByteBuf buf) {
        return new ExtractCurioToGridMessage(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ExtractCurioToGridMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractCurioToGrid(packet.identifier(), packet.index(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_GRID;
    }
}
