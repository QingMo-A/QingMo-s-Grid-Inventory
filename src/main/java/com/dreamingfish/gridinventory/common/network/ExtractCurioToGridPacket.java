package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExtractCurioToGridPacket(String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) implements CustomPacketPayload {
    public static final Type<ExtractCurioToGridPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "extract_curio_to_grid"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractCurioToGridPacket> STREAM_CODEC = StreamCodec.ofMember(ExtractCurioToGridPacket::encode, ExtractCurioToGridPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    private static ExtractCurioToGridPacket decode(RegistryFriendlyByteBuf buf) {
        return new ExtractCurioToGridPacket(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ExtractCurioToGridPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractCurioToGrid(packet.identifier(), packet.index(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
