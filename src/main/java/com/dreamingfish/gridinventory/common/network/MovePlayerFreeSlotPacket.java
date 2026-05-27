package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MovePlayerFreeSlotPacket(int sourcePlayerSlot, int targetPlayerSlot) implements CustomPacketPayload {
    public static final Type<MovePlayerFreeSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "move_player_free_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MovePlayerFreeSlotPacket> STREAM_CODEC = StreamCodec.ofMember(MovePlayerFreeSlotPacket::encode, MovePlayerFreeSlotPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sourcePlayerSlot);
        buf.writeVarInt(targetPlayerSlot);
    }

    private static MovePlayerFreeSlotPacket decode(RegistryFriendlyByteBuf buf) {
        return new MovePlayerFreeSlotPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(MovePlayerFreeSlotPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.movePlayerFreeSlot(packet.sourcePlayerSlot(), packet.targetPlayerSlot());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
