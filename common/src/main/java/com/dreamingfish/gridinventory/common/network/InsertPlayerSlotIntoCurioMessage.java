package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record InsertPlayerSlotIntoCurioMessage(int sourcePlayerSlot, String identifier, int index) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(sourcePlayerSlot);
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
    }

    public static InsertPlayerSlotIntoCurioMessage decode(FriendlyByteBuf buf) {
        return new InsertPlayerSlotIntoCurioMessage(buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(InsertPlayerSlotIntoCurioMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertPlayerSlotIntoCurio(packet.sourcePlayerSlot(), packet.identifier(), packet.index());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_PLAYER_SLOT_INTO_CURIO;
    }
}
