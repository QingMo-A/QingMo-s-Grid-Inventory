package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record DropEquipmentStorageEntryPacket(EquipmentSlot equipmentSlot, String containerId, UUID entryId) implements CustomPacketPayload {
    public static final Type<DropEquipmentStorageEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "drop_equipment_storage_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DropEquipmentStorageEntryPacket> STREAM_CODEC = StreamCodec.ofMember(DropEquipmentStorageEntryPacket::encode, DropEquipmentStorageEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
    }

    private static DropEquipmentStorageEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new DropEquipmentStorageEntryPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID());
    }

    public static void handle(DropEquipmentStorageEntryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.dropEquipmentStorageEntry(packet.equipmentSlot(), packet.containerId(), packet.entryId());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
