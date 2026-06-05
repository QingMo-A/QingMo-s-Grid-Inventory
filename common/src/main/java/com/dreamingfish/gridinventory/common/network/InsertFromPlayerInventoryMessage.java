package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record InsertFromPlayerInventoryMessage(int playerSlot, int targetX, int targetY, boolean rotated, boolean quick, boolean targetFolded) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(playerSlot);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(quick);
        buf.writeBoolean(targetFolded);
    }

    public static InsertFromPlayerInventoryMessage decode(FriendlyByteBuf buf) {
        return new InsertFromPlayerInventoryMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(InsertFromPlayerInventoryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            if (packet.quick()) {
                menu.quickInsertFromPlayerInventory(packet.playerSlot());
            } else {
                menu.insertFromPlayerInventory(packet.playerSlot(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            }
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_FROM_PLAYER_INVENTORY;
    }
}
