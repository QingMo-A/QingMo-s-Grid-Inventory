package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record QuickEquipPlayerSlotPacket(int playerSlot) implements CustomPacketPayload {
    public static final Type<QuickEquipPlayerSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "quick_equip_player_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickEquipPlayerSlotPacket> STREAM_CODEC = StreamCodec.ofMember(QuickEquipPlayerSlotPacket::encode, QuickEquipPlayerSlotPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(playerSlot);
    }

    private static QuickEquipPlayerSlotPacket decode(RegistryFriendlyByteBuf buf) {
        return new QuickEquipPlayerSlotPacket(buf.readVarInt());
    }

    public static void handle(QuickEquipPlayerSlotPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipPlayerSlot(packet.playerSlot());
            menu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
