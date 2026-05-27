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

public record TransferGridEntryIntoEquipmentStoragePacket(UUID entryId, EquipmentSlot equipmentSlot, String containerId,
                                                           int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<TransferGridEntryIntoEquipmentStoragePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "transfer_grid_entry_into_equipment_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TransferGridEntryIntoEquipmentStoragePacket> STREAM_CODEC = StreamCodec.ofMember(TransferGridEntryIntoEquipmentStoragePacket::encode, TransferGridEntryIntoEquipmentStoragePacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static TransferGridEntryIntoEquipmentStoragePacket decode(RegistryFriendlyByteBuf buf) {
        return new TransferGridEntryIntoEquipmentStoragePacket(buf.readUUID(), buf.readEnum(EquipmentSlot.class), buf.readUtf(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(TransferGridEntryIntoEquipmentStoragePacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferGridEntryIntoEquipmentStorage(packet.entryId(), packet.equipmentSlot(), packet.containerId(),
                    packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
