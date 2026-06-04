package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record ExtractToPlayerInventoryPacket(UUID entryId, int amount) implements CustomPacketPayload {
    public static final Type<ExtractToPlayerInventoryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "extract_to_player_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractToPlayerInventoryPacket> STREAM_CODEC = StreamCodec.ofMember(ExtractToPlayerInventoryPacket::encode, ExtractToPlayerInventoryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(amount);
    }

    private static ExtractToPlayerInventoryPacket decode(RegistryFriendlyByteBuf buf) {
        return new ExtractToPlayerInventoryPacket(buf.readUUID(), buf.readVarInt());
    }

    public static void handle(ExtractToPlayerInventoryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractToPlayerInventory(packet.entryId(), packet.amount());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
