package com.dreamingfish.gridinventory.fabric.platform.network;

import com.dreamingfish.gridinventory.common.network.SyncBackpackFoldingRulesPacket;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStoragePacket;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryPacket;
import com.dreamingfish.gridinventory.common.network.SyncItemSizeRulesPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricClientNetworking {
    private FabricClientNetworking() {
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncItemSizeRulesPacket.TYPE, (packet, context) -> context.client().execute(() -> SyncItemSizeRulesPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(SyncBackpackFoldingRulesPacket.TYPE, (packet, context) -> context.client().execute(() -> SyncBackpackFoldingRulesPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(SyncGridInventoryPacket.TYPE, (packet, context) -> context.client().execute(() -> SyncGridInventoryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(SyncEquipmentStoragePacket.TYPE, (packet, context) -> context.client().execute(() -> SyncEquipmentStoragePacket.handle(packet, new FabricGridPacketContext(context.player()))));
    }
}
