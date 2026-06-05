package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record MovePlayerFreeSlotMessage(int sourcePlayerSlot, int targetPlayerSlot) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(sourcePlayerSlot);
        buf.writeVarInt(targetPlayerSlot);
    }

    public static MovePlayerFreeSlotMessage decode(FriendlyByteBuf buf) {
        return new MovePlayerFreeSlotMessage(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(MovePlayerFreeSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.movePlayerFreeSlot(packet.sourcePlayerSlot(), packet.targetPlayerSlot());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_PLAYER_FREE_SLOT;
    }
}
