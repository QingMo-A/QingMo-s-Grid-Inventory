package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record ExtractCurioToPlayerSlotPacket(String identifier, int index, int targetPlayerSlot) implements CustomPacketPayload {
    public static final Type<ExtractCurioToPlayerSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "extract_curio_to_player_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractCurioToPlayerSlotPacket> STREAM_CODEC = StreamCodec.ofMember(ExtractCurioToPlayerSlotPacket::encode, ExtractCurioToPlayerSlotPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
        buf.writeVarInt(targetPlayerSlot);
    }

    private static ExtractCurioToPlayerSlotPacket decode(RegistryFriendlyByteBuf buf) {
        return new ExtractCurioToPlayerSlotPacket(buf.readUtf(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExtractCurioToPlayerSlotPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractCurioToPlayerSlot(packet.identifier(), packet.index(), packet.targetPlayerSlot());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
