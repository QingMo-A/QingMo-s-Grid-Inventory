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

public record ToggleEquipmentStorageEntryBackpackFoldPacket(EquipmentSlot slot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<ToggleEquipmentStorageEntryBackpackFoldPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "toggle_equipment_storage_entry_backpack_fold"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleEquipmentStorageEntryBackpackFoldPacket> STREAM_CODEC = StreamCodec.ofMember(ToggleEquipmentStorageEntryBackpackFoldPacket::encode, ToggleEquipmentStorageEntryBackpackFoldPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(slot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static ToggleEquipmentStorageEntryBackpackFoldPacket decode(RegistryFriendlyByteBuf buf) {
        return new ToggleEquipmentStorageEntryBackpackFoldPacket(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(ToggleEquipmentStorageEntryBackpackFoldPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.toggleEquipmentStorageEntryBackpackFold(packet.slot(), packet.containerId(), packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
