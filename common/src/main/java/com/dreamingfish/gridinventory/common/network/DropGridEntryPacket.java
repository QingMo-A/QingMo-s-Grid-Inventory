package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record DropGridEntryPacket(UUID entryId) implements CustomPacketPayload {
    public static final Type<DropGridEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "drop_grid_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DropGridEntryPacket> STREAM_CODEC = StreamCodec.ofMember(DropGridEntryPacket::encode, DropGridEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
    }

    private static DropGridEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new DropGridEntryPacket(buf.readUUID());
    }

    public static void handle(DropGridEntryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.dropGridEntry(packet.entryId());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
