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

public record TransferEquipmentStorageEntryIntoGridPacket(EquipmentSlot equipmentSlot, String containerId, UUID entryId,
                                                           int targetX, int targetY, boolean rotated, boolean targetFolded) implements CustomPacketPayload {
    public static final Type<TransferEquipmentStorageEntryIntoGridPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "transfer_equipment_storage_entry_into_grid"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TransferEquipmentStorageEntryIntoGridPacket> STREAM_CODEC = StreamCodec.ofMember(TransferEquipmentStorageEntryIntoGridPacket::encode, TransferEquipmentStorageEntryIntoGridPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    private static TransferEquipmentStorageEntryIntoGridPacket decode(RegistryFriendlyByteBuf buf) {
        return new TransferEquipmentStorageEntryIntoGridPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(TransferEquipmentStorageEntryIntoGridPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferEquipmentEntryIntoGrid(packet.equipmentSlot(), packet.containerId(), packet.entryId(),
                    packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
