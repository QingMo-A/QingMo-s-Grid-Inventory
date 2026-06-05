package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.inventory.PlayerGridInventoryOpener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record OpenPlayerGridInventoryPacket() implements CustomPacketPayload {
    public static final OpenPlayerGridInventoryPacket INSTANCE = new OpenPlayerGridInventoryPacket();
    public static final Type<OpenPlayerGridInventoryPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "open_player_grid_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPlayerGridInventoryPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(OpenPlayerGridInventoryPacket packet, GridPacketContext context) {
        if (GridInventoryServices.config().replaceSurvivalInventory() && context.player() instanceof ServerPlayer player && !player.isCreative()) {
            PlayerGridInventoryOpener.open(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
