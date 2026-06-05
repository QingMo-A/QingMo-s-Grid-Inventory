package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record ExtractCurioToPlayerSlotMessage(String identifier, int index, int targetPlayerSlot) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
        buf.writeVarInt(targetPlayerSlot);
    }

    public static ExtractCurioToPlayerSlotMessage decode(FriendlyByteBuf buf) {
        return new ExtractCurioToPlayerSlotMessage(buf.readUtf(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExtractCurioToPlayerSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractCurioToPlayerSlot(packet.identifier(), packet.index(), packet.targetPlayerSlot());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_PLAYER_SLOT;
    }
}
