package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InsertIntoEquipmentStoragePacket(int playerSlot, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<InsertIntoEquipmentStoragePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "insert_into_equipment_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertIntoEquipmentStoragePacket> STREAM_CODEC = StreamCodec.ofMember(InsertIntoEquipmentStoragePacket::encode, InsertIntoEquipmentStoragePacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(playerSlot);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static InsertIntoEquipmentStoragePacket decode(RegistryFriendlyByteBuf buf) {
        return new InsertIntoEquipmentStoragePacket(buf.readVarInt(), buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(InsertIntoEquipmentStoragePacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertFromPlayerIntoEquipmentStorage(packet.playerSlot(), packet.equipmentSlot(), packet.containerId(), packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
