package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record InsertFromPlayerInventoryPacket(int playerSlot, int targetX, int targetY, boolean rotated, boolean quick, boolean targetFolded) implements CustomPacketPayload {
    public static final Type<InsertFromPlayerInventoryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "insert_from_player_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertFromPlayerInventoryPacket> STREAM_CODEC = StreamCodec.ofMember(InsertFromPlayerInventoryPacket::encode, InsertFromPlayerInventoryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(playerSlot);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(quick);
        buf.writeBoolean(targetFolded);
    }

    private static InsertFromPlayerInventoryPacket decode(RegistryFriendlyByteBuf buf) {
        return new InsertFromPlayerInventoryPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(InsertFromPlayerInventoryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            if (packet.quick()) {
                menu.quickInsertFromPlayerInventory(packet.playerSlot());
            } else {
                menu.insertFromPlayerInventory(packet.playerSlot(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            }
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
