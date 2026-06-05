package com.dreamingfish.gridinventory.fabric.platform.network;

import com.dreamingfish.gridinventory.common.network.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricNetworking {
    private FabricNetworking() {
    }

    public static void registerCommonPackets() {
        PayloadTypeRegistry.playS2C().register(SyncItemSizeRulesPacket.TYPE, SyncItemSizeRulesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncBackpackFoldingRulesPacket.TYPE, SyncBackpackFoldingRulesPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncGridInventoryPacket.TYPE, SyncGridInventoryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEquipmentStoragePacket.TYPE, SyncEquipmentStoragePacket.STREAM_CODEC);

        PayloadTypeRegistry.playC2S().register(MoveGridEntryPacket.TYPE, MoveGridEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleGridEntryBackpackFoldPacket.TYPE, ToggleGridEntryBackpackFoldPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleEquipmentStorageEntryBackpackFoldPacket.TYPE, ToggleEquipmentStorageEntryBackpackFoldPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(InsertFromPlayerInventoryPacket.TYPE, InsertFromPlayerInventoryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExtractToPlayerInventoryPacket.TYPE, ExtractToPlayerInventoryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExtractGridEntryToPlayerSlotPacket.TYPE, ExtractGridEntryToPlayerSlotPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(OpenPlayerGridInventoryPacket.TYPE, OpenPlayerGridInventoryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ManualPickupItemPacket.TYPE, ManualPickupItemPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PickupGroundItemIntoGridPacket.TYPE, PickupGroundItemIntoGridPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PickupGroundItemIntoEquipmentStoragePacket.TYPE, PickupGroundItemIntoEquipmentStoragePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(InsertIntoEquipmentStoragePacket.TYPE, InsertIntoEquipmentStoragePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(MoveEquipmentStorageEntryPacket.TYPE, MoveEquipmentStorageEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExtractEquipmentStorageEntryPacket.TYPE, ExtractEquipmentStorageEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(TransferGridEntryIntoEquipmentStoragePacket.TYPE, TransferGridEntryIntoEquipmentStoragePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(TransferEquipmentStorageEntryIntoGridPacket.TYPE, TransferEquipmentStorageEntryIntoGridPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(TransferEquipmentStorageEntryPacket.TYPE, TransferEquipmentStorageEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(MovePlayerFreeSlotPacket.TYPE, MovePlayerFreeSlotPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuickEquipGridEntryPacket.TYPE, QuickEquipGridEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuickEquipEquipmentStorageEntryPacket.TYPE, QuickEquipEquipmentStorageEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(QuickEquipPlayerSlotPacket.TYPE, QuickEquipPlayerSlotPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DropGridEntryPacket.TYPE, DropGridEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DropEquipmentStorageEntryPacket.TYPE, DropEquipmentStorageEntryPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(InsertPlayerSlotIntoCurioPacket.TYPE, InsertPlayerSlotIntoCurioPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(InsertGridEntryIntoCurioPacket.TYPE, InsertGridEntryIntoCurioPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(InsertEquipmentStorageEntryIntoCurioPacket.TYPE, InsertEquipmentStorageEntryIntoCurioPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExtractCurioToPlayerSlotPacket.TYPE, ExtractCurioToPlayerSlotPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExtractCurioToGridPacket.TYPE, ExtractCurioToGridPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ExtractCurioToEquipmentStoragePacket.TYPE, ExtractCurioToEquipmentStoragePacket.STREAM_CODEC);
    }

    public static void registerServerReceivers() {
        ServerLifecycleEvents.SERVER_STARTED.register(FabricGridInventoryNetworkBridge::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> FabricGridInventoryNetworkBridge.setCurrentServer(null));

        ServerPlayNetworking.registerGlobalReceiver(MoveGridEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> MoveGridEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleGridEntryBackpackFoldPacket.TYPE, (packet, context) -> context.server().execute(() -> ToggleGridEntryBackpackFoldPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ToggleEquipmentStorageEntryBackpackFoldPacket.TYPE, (packet, context) -> context.server().execute(() -> ToggleEquipmentStorageEntryBackpackFoldPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(InsertFromPlayerInventoryPacket.TYPE, (packet, context) -> context.server().execute(() -> InsertFromPlayerInventoryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ExtractToPlayerInventoryPacket.TYPE, (packet, context) -> context.server().execute(() -> ExtractToPlayerInventoryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ExtractGridEntryToPlayerSlotPacket.TYPE, (packet, context) -> context.server().execute(() -> ExtractGridEntryToPlayerSlotPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(OpenPlayerGridInventoryPacket.TYPE, (packet, context) -> context.server().execute(() -> OpenPlayerGridInventoryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ManualPickupItemPacket.TYPE, (packet, context) -> context.server().execute(() -> ManualPickupItemPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(PickupGroundItemIntoGridPacket.TYPE, (packet, context) -> context.server().execute(() -> PickupGroundItemIntoGridPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(PickupGroundItemIntoEquipmentStoragePacket.TYPE, (packet, context) -> context.server().execute(() -> PickupGroundItemIntoEquipmentStoragePacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(InsertIntoEquipmentStoragePacket.TYPE, (packet, context) -> context.server().execute(() -> InsertIntoEquipmentStoragePacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(MoveEquipmentStorageEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> MoveEquipmentStorageEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ExtractEquipmentStorageEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> ExtractEquipmentStorageEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(TransferGridEntryIntoEquipmentStoragePacket.TYPE, (packet, context) -> context.server().execute(() -> TransferGridEntryIntoEquipmentStoragePacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(TransferEquipmentStorageEntryIntoGridPacket.TYPE, (packet, context) -> context.server().execute(() -> TransferEquipmentStorageEntryIntoGridPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(TransferEquipmentStorageEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> TransferEquipmentStorageEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(MovePlayerFreeSlotPacket.TYPE, (packet, context) -> context.server().execute(() -> MovePlayerFreeSlotPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuickEquipGridEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> QuickEquipGridEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuickEquipEquipmentStorageEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> QuickEquipEquipmentStorageEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(QuickEquipPlayerSlotPacket.TYPE, (packet, context) -> context.server().execute(() -> QuickEquipPlayerSlotPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(DropGridEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> DropGridEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(DropEquipmentStorageEntryPacket.TYPE, (packet, context) -> context.server().execute(() -> DropEquipmentStorageEntryPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(InsertPlayerSlotIntoCurioPacket.TYPE, (packet, context) -> context.server().execute(() -> InsertPlayerSlotIntoCurioPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(InsertGridEntryIntoCurioPacket.TYPE, (packet, context) -> context.server().execute(() -> InsertGridEntryIntoCurioPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(InsertEquipmentStorageEntryIntoCurioPacket.TYPE, (packet, context) -> context.server().execute(() -> InsertEquipmentStorageEntryIntoCurioPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ExtractCurioToPlayerSlotPacket.TYPE, (packet, context) -> context.server().execute(() -> ExtractCurioToPlayerSlotPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ExtractCurioToGridPacket.TYPE, (packet, context) -> context.server().execute(() -> ExtractCurioToGridPacket.handle(packet, new FabricGridPacketContext(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(ExtractCurioToEquipmentStoragePacket.TYPE, (packet, context) -> context.server().execute(() -> ExtractCurioToEquipmentStoragePacket.handle(packet, new FabricGridPacketContext(context.player()))));
    }
}
