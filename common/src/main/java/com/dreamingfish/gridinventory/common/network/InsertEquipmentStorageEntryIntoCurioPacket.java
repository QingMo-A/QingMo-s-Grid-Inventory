package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record InsertEquipmentStorageEntryIntoCurioPacket(EquipmentSlot sourceSlot, String containerId, UUID entryId,
                                                         String identifier, int index) implements CustomPacketPayload {
    public static final Type<InsertEquipmentStorageEntryIntoCurioPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "insert_equipment_storage_entry_into_curio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertEquipmentStorageEntryIntoCurioPacket> STREAM_CODEC = StreamCodec.ofMember(InsertEquipmentStorageEntryIntoCurioPacket::encode, InsertEquipmentStorageEntryIntoCurioPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(sourceSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
    }

    private static InsertEquipmentStorageEntryIntoCurioPacket decode(RegistryFriendlyByteBuf buf) {
        return new InsertEquipmentStorageEntryIntoCurioPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(InsertEquipmentStorageEntryIntoCurioPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertEquipmentStorageEntryIntoCurio(packet.sourceSlot(), packet.containerId(), packet.entryId(), packet.identifier(), packet.index());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
