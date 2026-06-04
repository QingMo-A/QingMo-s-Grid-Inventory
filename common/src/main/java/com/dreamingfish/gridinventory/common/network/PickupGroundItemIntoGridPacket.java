package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record PickupGroundItemIntoGridPacket(int entityId, int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<PickupGroundItemIntoGridPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "pickup_ground_item_into_grid"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PickupGroundItemIntoGridPacket> STREAM_CODEC = StreamCodec.ofMember(PickupGroundItemIntoGridPacket::encode, PickupGroundItemIntoGridPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static PickupGroundItemIntoGridPacket decode(RegistryFriendlyByteBuf buf) {
        return new PickupGroundItemIntoGridPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(PickupGroundItemIntoGridPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.pickupGroundItemIntoGrid(packet.entityId(), packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
