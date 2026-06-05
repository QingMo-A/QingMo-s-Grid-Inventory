package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

public record PickupGroundItemIntoEquipmentStorageMessage(int entityId, EquipmentSlot equipmentSlot, String containerId,
                                                         int targetX, int targetY, boolean rotated) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    public static PickupGroundItemIntoEquipmentStorageMessage decode(FriendlyByteBuf buf) {
        return new PickupGroundItemIntoEquipmentStorageMessage(buf.readVarInt(), buf.readEnum(EquipmentSlot.class), buf.readUtf(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(PickupGroundItemIntoEquipmentStorageMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.pickupGroundItemIntoEquipmentStorage(packet.entityId(), packet.equipmentSlot(), packet.containerId(),
                    packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_EQUIPMENT_STORAGE;
    }
}
