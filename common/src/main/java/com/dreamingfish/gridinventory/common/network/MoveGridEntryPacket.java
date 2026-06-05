package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record MoveGridEntryPacket(UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements CustomPacketPayload {
    public static final Type<MoveGridEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "move_grid_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveGridEntryPacket> STREAM_CODEC = StreamCodec.ofMember(MoveGridEntryPacket::encode, MoveGridEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    private static MoveGridEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new MoveGridEntryPacket(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(MoveGridEntryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.moveEntry(packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
