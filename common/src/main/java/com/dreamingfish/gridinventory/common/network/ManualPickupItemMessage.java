package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.pickup.ManualPickupHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record ManualPickupItemMessage(int entityId) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    public static ManualPickupItemMessage decode(FriendlyByteBuf buf) {
        return new ManualPickupItemMessage(buf.readVarInt());
    }

    public static void handle(ManualPickupItemMessage packet, GridMessageContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ManualPickupHandler.tryPickupToPlayerInventory(player, packet.entityId());
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MANUAL_PICKUP_ITEM;
    }
}
