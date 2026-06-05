package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record PickupGroundItemIntoEquipmentStoragePacket(int entityId, EquipmentSlot equipmentSlot, String containerId,
                                                         int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<PickupGroundItemIntoEquipmentStoragePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "pickup_ground_item_into_equipment_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupGroundItemIntoEquipmentStoragePacket> STREAM_CODEC = StreamCodec.ofMember(PickupGroundItemIntoEquipmentStoragePacket::encode, PickupGroundItemIntoEquipmentStoragePacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static PickupGroundItemIntoEquipmentStoragePacket decode(RegistryFriendlyByteBuf buf) {
        return new PickupGroundItemIntoEquipmentStoragePacket(buf.readVarInt(), buf.readEnum(EquipmentSlot.class), buf.readUtf(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(PickupGroundItemIntoEquipmentStoragePacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.pickupGroundItemIntoEquipmentStorage(packet.entityId(), packet.equipmentSlot(), packet.containerId(),
                    packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
