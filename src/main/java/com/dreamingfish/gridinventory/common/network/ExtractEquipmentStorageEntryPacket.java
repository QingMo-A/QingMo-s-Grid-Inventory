package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ExtractEquipmentStorageEntryPacket(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int playerSlot, int amount) implements CustomPacketPayload {
    public static final Type<ExtractEquipmentStorageEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "extract_equipment_storage_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractEquipmentStorageEntryPacket> STREAM_CODEC = StreamCodec.ofMember(ExtractEquipmentStorageEntryPacket::encode, ExtractEquipmentStorageEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeVarInt(playerSlot);
        buf.writeVarInt(amount);
    }

    private static ExtractEquipmentStorageEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new ExtractEquipmentStorageEntryPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExtractEquipmentStorageEntryPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractEquipmentEntryToPlayerSlot(packet.equipmentSlot(), packet.containerId(), packet.entryId(), packet.playerSlot(), packet.amount());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
