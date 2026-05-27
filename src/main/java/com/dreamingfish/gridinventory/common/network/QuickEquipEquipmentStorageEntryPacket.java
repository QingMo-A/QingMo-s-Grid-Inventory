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

public record QuickEquipEquipmentStorageEntryPacket(EquipmentSlot sourceSlot, String containerId, UUID entryId) implements CustomPacketPayload {
    public static final Type<QuickEquipEquipmentStorageEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "quick_equip_equipment_storage_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickEquipEquipmentStorageEntryPacket> STREAM_CODEC = StreamCodec.ofMember(QuickEquipEquipmentStorageEntryPacket::encode, QuickEquipEquipmentStorageEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(sourceSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
    }

    private static QuickEquipEquipmentStorageEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new QuickEquipEquipmentStorageEntryPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID());
    }

    public static void handle(QuickEquipEquipmentStorageEntryPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipEquipmentStorageEntry(packet.sourceSlot(), packet.containerId(), packet.entryId());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
