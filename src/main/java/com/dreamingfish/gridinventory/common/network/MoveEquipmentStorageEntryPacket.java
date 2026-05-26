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

public record MoveEquipmentStorageEntryPacket(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<MoveEquipmentStorageEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "move_equipment_storage_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveEquipmentStorageEntryPacket> STREAM_CODEC = StreamCodec.ofMember(MoveEquipmentStorageEntryPacket::encode, MoveEquipmentStorageEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static MoveEquipmentStorageEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new MoveEquipmentStorageEntryPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MoveEquipmentStorageEntryPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.moveEquipmentEntry(packet.equipmentSlot(), packet.containerId(), packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
