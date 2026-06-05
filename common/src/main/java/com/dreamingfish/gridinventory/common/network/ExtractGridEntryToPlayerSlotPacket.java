package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record ExtractGridEntryToPlayerSlotPacket(UUID entryId, int playerSlot, int amount) implements CustomPacketPayload {
    public static final Type<ExtractGridEntryToPlayerSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "extract_grid_entry_to_player_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractGridEntryToPlayerSlotPacket> STREAM_CODEC = StreamCodec.ofMember(ExtractGridEntryToPlayerSlotPacket::encode, ExtractGridEntryToPlayerSlotPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(playerSlot);
        buf.writeVarInt(amount);
    }

    private static ExtractGridEntryToPlayerSlotPacket decode(RegistryFriendlyByteBuf buf) {
        return new ExtractGridEntryToPlayerSlotPacket(buf.readUUID(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExtractGridEntryToPlayerSlotPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractToPlayerSlot(packet.entryId(), packet.playerSlot(), packet.amount());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
