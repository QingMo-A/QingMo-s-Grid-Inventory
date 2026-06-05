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

public record TransferEquipmentStorageEntryPacket(EquipmentSlot sourceSlot, String sourceContainerId, UUID entryId,
                                                  EquipmentSlot targetSlot, String targetContainerId,
                                                  int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<TransferEquipmentStorageEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "transfer_equipment_storage_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TransferEquipmentStorageEntryPacket> STREAM_CODEC = StreamCodec.ofMember(TransferEquipmentStorageEntryPacket::encode, TransferEquipmentStorageEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(sourceSlot);
        buf.writeUtf(sourceContainerId);
        buf.writeUUID(entryId);
        buf.writeEnum(targetSlot);
        buf.writeUtf(targetContainerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static TransferEquipmentStorageEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new TransferEquipmentStorageEntryPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(TransferEquipmentStorageEntryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferEquipmentEntryBetweenStorages(packet.sourceSlot(), packet.sourceContainerId(), packet.entryId(),
                    packet.targetSlot(), packet.targetContainerId(), packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
