package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExtractCurioToEquipmentStoragePacket(String identifier, int index, EquipmentSlot equipmentSlot,
                                                   String containerId, int targetX, int targetY,
                                                   boolean rotated) implements CustomPacketPayload {
    public static final Type<ExtractCurioToEquipmentStoragePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "extract_curio_to_equipment_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractCurioToEquipmentStoragePacket> STREAM_CODEC = StreamCodec.ofMember(ExtractCurioToEquipmentStoragePacket::encode, ExtractCurioToEquipmentStoragePacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static ExtractCurioToEquipmentStoragePacket decode(RegistryFriendlyByteBuf buf) {
        return new ExtractCurioToEquipmentStoragePacket(buf.readUtf(), buf.readVarInt(), buf.readEnum(EquipmentSlot.class),
                buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(ExtractCurioToEquipmentStoragePacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertCurioIntoEquipmentStorage(packet.identifier(), packet.index(), packet.equipmentSlot(), packet.containerId(),
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
