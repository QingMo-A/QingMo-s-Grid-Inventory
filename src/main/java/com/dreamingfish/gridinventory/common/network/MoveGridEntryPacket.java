package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record MoveGridEntryPacket(UUID entryId, int targetX, int targetY, boolean rotated) implements CustomPacketPayload {
    public static final Type<MoveGridEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "move_grid_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MoveGridEntryPacket> STREAM_CODEC = StreamCodec.ofMember(MoveGridEntryPacket::encode, MoveGridEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    private static MoveGridEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new MoveGridEntryPacket(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MoveGridEntryPacket packet, IPayloadContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.moveEntry(packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
