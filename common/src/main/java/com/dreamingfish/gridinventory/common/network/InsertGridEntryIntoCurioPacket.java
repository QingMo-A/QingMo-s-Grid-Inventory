package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record InsertGridEntryIntoCurioPacket(UUID entryId, String identifier, int index) implements CustomPacketPayload {
    public static final Type<InsertGridEntryIntoCurioPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "insert_grid_entry_into_curio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertGridEntryIntoCurioPacket> STREAM_CODEC = StreamCodec.ofMember(InsertGridEntryIntoCurioPacket::encode, InsertGridEntryIntoCurioPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
    }

    private static InsertGridEntryIntoCurioPacket decode(RegistryFriendlyByteBuf buf) {
        return new InsertGridEntryIntoCurioPacket(buf.readUUID(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(InsertGridEntryIntoCurioPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertGridEntryIntoCurio(packet.entryId(), packet.identifier(), packet.index());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
