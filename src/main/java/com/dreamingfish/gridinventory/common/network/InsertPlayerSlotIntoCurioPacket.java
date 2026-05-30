package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InsertPlayerSlotIntoCurioPacket(int sourcePlayerSlot, String identifier, int index) implements CustomPacketPayload {
    public static final Type<InsertPlayerSlotIntoCurioPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "insert_player_slot_into_curio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertPlayerSlotIntoCurioPacket> STREAM_CODEC = StreamCodec.ofMember(InsertPlayerSlotIntoCurioPacket::encode, InsertPlayerSlotIntoCurioPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sourcePlayerSlot);
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
    }

    private static InsertPlayerSlotIntoCurioPacket decode(RegistryFriendlyByteBuf buf) {
        return new InsertPlayerSlotIntoCurioPacket(buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(InsertPlayerSlotIntoCurioPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertPlayerSlotIntoCurio(packet.sourcePlayerSlot(), packet.identifier(), packet.index());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
