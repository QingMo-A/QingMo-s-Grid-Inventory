package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.pickup.ManualPickupHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ManualPickupItemPacket(int entityId) implements CustomPacketPayload {
    public static final Type<ManualPickupItemPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "manual_pickup_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ManualPickupItemPacket> STREAM_CODEC = StreamCodec.ofMember(ManualPickupItemPacket::encode, ManualPickupItemPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
    }

    private static ManualPickupItemPacket decode(RegistryFriendlyByteBuf buf) {
        return new ManualPickupItemPacket(buf.readVarInt());
    }

    public static void handle(ManualPickupItemPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ManualPickupHandler.tryPickupToPlayerInventory(player, packet.entityId());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
