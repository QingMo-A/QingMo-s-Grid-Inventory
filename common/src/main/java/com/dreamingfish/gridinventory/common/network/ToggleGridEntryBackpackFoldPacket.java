package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record ToggleGridEntryBackpackFoldPacket(UUID entryId, int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<ToggleGridEntryBackpackFoldPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "toggle_grid_entry_backpack_fold"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleGridEntryBackpackFoldPacket> STREAM_CODEC = StreamCodec.ofMember(ToggleGridEntryBackpackFoldPacket::encode, ToggleGridEntryBackpackFoldPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static ToggleGridEntryBackpackFoldPacket decode(RegistryFriendlyByteBuf buf) {
        return new ToggleGridEntryBackpackFoldPacket(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(ToggleGridEntryBackpackFoldPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.toggleGridEntryBackpackFold(packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
