package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record SyncGridInventoryPacket(GridInventoryData data) implements CustomPacketPayload {
    public static final Type<SyncGridInventoryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "sync_grid_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGridInventoryPacket> STREAM_CODEC = StreamCodec.ofMember(SyncGridInventoryPacket::encode, SyncGridInventoryPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        data.encode(buf);
    }

    private static SyncGridInventoryPacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncGridInventoryPacket(GridInventoryData.decode(buf));
    }

    public static void handle(SyncGridInventoryPacket packet, GridPacketContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.replaceGridData(packet.data());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
