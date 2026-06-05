package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.UUID;

public record QuickEquipGridEntryPacket(UUID entryId) implements CustomPacketPayload {
    public static final Type<QuickEquipGridEntryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "quick_equip_grid_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickEquipGridEntryPacket> STREAM_CODEC = StreamCodec.ofMember(QuickEquipGridEntryPacket::encode, QuickEquipGridEntryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
    }

    private static QuickEquipGridEntryPacket decode(RegistryFriendlyByteBuf buf) {
        return new QuickEquipGridEntryPacket(buf.readUUID());
    }

    public static void handle(QuickEquipGridEntryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipGridEntry(packet.entryId());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
