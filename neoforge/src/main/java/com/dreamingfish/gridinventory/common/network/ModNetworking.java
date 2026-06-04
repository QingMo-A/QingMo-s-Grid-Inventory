package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.platform.neoforge.NeoForgeGridPacketContext;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncItemSizeRulesPacket.TYPE, SyncItemSizeRulesPacket.STREAM_CODEC, (packet, context) -> SyncItemSizeRulesPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToClient(SyncBackpackFoldingRulesPacket.TYPE, SyncBackpackFoldingRulesPacket.STREAM_CODEC, (packet, context) -> SyncBackpackFoldingRulesPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToClient(SyncGridInventoryPacket.TYPE, SyncGridInventoryPacket.STREAM_CODEC, (packet, context) -> SyncGridInventoryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToClient(SyncEquipmentStoragePacket.TYPE, SyncEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> SyncEquipmentStoragePacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(MoveGridEntryPacket.TYPE, MoveGridEntryPacket.STREAM_CODEC, (packet, context) -> MoveGridEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ToggleGridEntryBackpackFoldPacket.TYPE, ToggleGridEntryBackpackFoldPacket.STREAM_CODEC, (packet, context) -> ToggleGridEntryBackpackFoldPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ToggleEquipmentStorageEntryBackpackFoldPacket.TYPE, ToggleEquipmentStorageEntryBackpackFoldPacket.STREAM_CODEC, (packet, context) -> ToggleEquipmentStorageEntryBackpackFoldPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(InsertFromPlayerInventoryPacket.TYPE, InsertFromPlayerInventoryPacket.STREAM_CODEC, (packet, context) -> InsertFromPlayerInventoryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ExtractToPlayerInventoryPacket.TYPE, ExtractToPlayerInventoryPacket.STREAM_CODEC, (packet, context) -> ExtractToPlayerInventoryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ExtractGridEntryToPlayerSlotPacket.TYPE, ExtractGridEntryToPlayerSlotPacket.STREAM_CODEC, (packet, context) -> ExtractGridEntryToPlayerSlotPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(OpenPlayerGridInventoryPacket.TYPE, OpenPlayerGridInventoryPacket.STREAM_CODEC, (packet, context) -> OpenPlayerGridInventoryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ManualPickupItemPacket.TYPE, ManualPickupItemPacket.STREAM_CODEC, (packet, context) -> ManualPickupItemPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(PickupGroundItemIntoGridPacket.TYPE, PickupGroundItemIntoGridPacket.STREAM_CODEC, (packet, context) -> PickupGroundItemIntoGridPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(PickupGroundItemIntoEquipmentStoragePacket.TYPE, PickupGroundItemIntoEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> PickupGroundItemIntoEquipmentStoragePacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(InsertIntoEquipmentStoragePacket.TYPE, InsertIntoEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> InsertIntoEquipmentStoragePacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(MoveEquipmentStorageEntryPacket.TYPE, MoveEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> MoveEquipmentStorageEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ExtractEquipmentStorageEntryPacket.TYPE, ExtractEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> ExtractEquipmentStorageEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(TransferGridEntryIntoEquipmentStoragePacket.TYPE, TransferGridEntryIntoEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> TransferGridEntryIntoEquipmentStoragePacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(TransferEquipmentStorageEntryIntoGridPacket.TYPE, TransferEquipmentStorageEntryIntoGridPacket.STREAM_CODEC, (packet, context) -> TransferEquipmentStorageEntryIntoGridPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(TransferEquipmentStorageEntryPacket.TYPE, TransferEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> TransferEquipmentStorageEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(MovePlayerFreeSlotPacket.TYPE, MovePlayerFreeSlotPacket.STREAM_CODEC, (packet, context) -> MovePlayerFreeSlotPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(QuickEquipGridEntryPacket.TYPE, QuickEquipGridEntryPacket.STREAM_CODEC, (packet, context) -> QuickEquipGridEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(QuickEquipEquipmentStorageEntryPacket.TYPE, QuickEquipEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> QuickEquipEquipmentStorageEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(QuickEquipPlayerSlotPacket.TYPE, QuickEquipPlayerSlotPacket.STREAM_CODEC, (packet, context) -> QuickEquipPlayerSlotPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(DropGridEntryPacket.TYPE, DropGridEntryPacket.STREAM_CODEC, (packet, context) -> DropGridEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(DropEquipmentStorageEntryPacket.TYPE, DropEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> DropEquipmentStorageEntryPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(InsertPlayerSlotIntoCurioPacket.TYPE, InsertPlayerSlotIntoCurioPacket.STREAM_CODEC, (packet, context) -> InsertPlayerSlotIntoCurioPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(InsertGridEntryIntoCurioPacket.TYPE, InsertGridEntryIntoCurioPacket.STREAM_CODEC, (packet, context) -> InsertGridEntryIntoCurioPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(InsertEquipmentStorageEntryIntoCurioPacket.TYPE, InsertEquipmentStorageEntryIntoCurioPacket.STREAM_CODEC, (packet, context) -> InsertEquipmentStorageEntryIntoCurioPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ExtractCurioToPlayerSlotPacket.TYPE, ExtractCurioToPlayerSlotPacket.STREAM_CODEC, (packet, context) -> ExtractCurioToPlayerSlotPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ExtractCurioToGridPacket.TYPE, ExtractCurioToGridPacket.STREAM_CODEC, (packet, context) -> ExtractCurioToGridPacket.handle(packet, new NeoForgeGridPacketContext(context)));
        registrar.playToServer(ExtractCurioToEquipmentStoragePacket.TYPE, ExtractCurioToEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> ExtractCurioToEquipmentStoragePacket.handle(packet, new NeoForgeGridPacketContext(context)));
    }

    public static void syncMenu(Player player, GridInventoryMenu menu) {
        GridInventoryServices.network().syncMenu(player, menu);
    }

    public static void syncEquipmentStorage(Player player, net.minecraft.world.entity.EquipmentSlot slot, com.dreamingfish.gridinventory.common.data.EquipmentStorageData storage) {
        GridInventoryServices.network().syncEquipmentStorage(player, slot, storage);
    }
}
