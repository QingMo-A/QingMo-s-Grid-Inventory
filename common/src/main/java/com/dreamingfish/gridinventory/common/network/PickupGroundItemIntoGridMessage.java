package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record PickupGroundItemIntoGridMessage(int entityId, int targetX, int targetY, boolean rotated) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    public static PickupGroundItemIntoGridMessage decode(FriendlyByteBuf buf) {
        return new PickupGroundItemIntoGridMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(PickupGroundItemIntoGridMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.pickupGroundItemIntoGrid(packet.entityId(), packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_GRID;
    }
}
