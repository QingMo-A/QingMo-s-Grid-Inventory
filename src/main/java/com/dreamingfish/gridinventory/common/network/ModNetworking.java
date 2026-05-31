package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncItemSizeRulesPacket.TYPE, SyncItemSizeRulesPacket.STREAM_CODEC, SyncItemSizeRulesPacket::handle);
        registrar.playToClient(SyncGridInventoryPacket.TYPE, SyncGridInventoryPacket.STREAM_CODEC, SyncGridInventoryPacket::handle);
        registrar.playToClient(SyncEquipmentStoragePacket.TYPE, SyncEquipmentStoragePacket.STREAM_CODEC, SyncEquipmentStoragePacket::handle);
        registrar.playToServer(MoveGridEntryPacket.TYPE, MoveGridEntryPacket.STREAM_CODEC, MoveGridEntryPacket::handle);
        registrar.playToServer(InsertFromPlayerInventoryPacket.TYPE, InsertFromPlayerInventoryPacket.STREAM_CODEC, InsertFromPlayerInventoryPacket::handle);
        registrar.playToServer(ExtractToPlayerInventoryPacket.TYPE, ExtractToPlayerInventoryPacket.STREAM_CODEC, ExtractToPlayerInventoryPacket::handle);
        registrar.playToServer(ExtractGridEntryToPlayerSlotPacket.TYPE, ExtractGridEntryToPlayerSlotPacket.STREAM_CODEC, ExtractGridEntryToPlayerSlotPacket::handle);
        registrar.playToServer(OpenPlayerGridInventoryPacket.TYPE, OpenPlayerGridInventoryPacket.STREAM_CODEC, OpenPlayerGridInventoryPacket::handle);
        registrar.playToServer(ManualPickupItemPacket.TYPE, ManualPickupItemPacket.STREAM_CODEC, ManualPickupItemPacket::handle);
        registrar.playToServer(PickupGroundItemIntoGridPacket.TYPE, PickupGroundItemIntoGridPacket.STREAM_CODEC, PickupGroundItemIntoGridPacket::handle);
        registrar.playToServer(PickupGroundItemIntoEquipmentStoragePacket.TYPE, PickupGroundItemIntoEquipmentStoragePacket.STREAM_CODEC, PickupGroundItemIntoEquipmentStoragePacket::handle);
        registrar.playToServer(InsertIntoEquipmentStoragePacket.TYPE, InsertIntoEquipmentStoragePacket.STREAM_CODEC, InsertIntoEquipmentStoragePacket::handle);
        registrar.playToServer(MoveEquipmentStorageEntryPacket.TYPE, MoveEquipmentStorageEntryPacket.STREAM_CODEC, MoveEquipmentStorageEntryPacket::handle);
        registrar.playToServer(ExtractEquipmentStorageEntryPacket.TYPE, ExtractEquipmentStorageEntryPacket.STREAM_CODEC, ExtractEquipmentStorageEntryPacket::handle);
        registrar.playToServer(TransferGridEntryIntoEquipmentStoragePacket.TYPE, TransferGridEntryIntoEquipmentStoragePacket.STREAM_CODEC, TransferGridEntryIntoEquipmentStoragePacket::handle);
        registrar.playToServer(TransferEquipmentStorageEntryIntoGridPacket.TYPE, TransferEquipmentStorageEntryIntoGridPacket.STREAM_CODEC, TransferEquipmentStorageEntryIntoGridPacket::handle);
        registrar.playToServer(TransferEquipmentStorageEntryPacket.TYPE, TransferEquipmentStorageEntryPacket.STREAM_CODEC, TransferEquipmentStorageEntryPacket::handle);
        registrar.playToServer(MovePlayerFreeSlotPacket.TYPE, MovePlayerFreeSlotPacket.STREAM_CODEC, MovePlayerFreeSlotPacket::handle);
        registrar.playToServer(QuickEquipGridEntryPacket.TYPE, QuickEquipGridEntryPacket.STREAM_CODEC, QuickEquipGridEntryPacket::handle);
        registrar.playToServer(QuickEquipEquipmentStorageEntryPacket.TYPE, QuickEquipEquipmentStorageEntryPacket.STREAM_CODEC, QuickEquipEquipmentStorageEntryPacket::handle);
        registrar.playToServer(QuickEquipPlayerSlotPacket.TYPE, QuickEquipPlayerSlotPacket.STREAM_CODEC, QuickEquipPlayerSlotPacket::handle);
        registrar.playToServer(DropGridEntryPacket.TYPE, DropGridEntryPacket.STREAM_CODEC, DropGridEntryPacket::handle);
        registrar.playToServer(DropEquipmentStorageEntryPacket.TYPE, DropEquipmentStorageEntryPacket.STREAM_CODEC, DropEquipmentStorageEntryPacket::handle);
        registrar.playToServer(InsertPlayerSlotIntoCurioPacket.TYPE, InsertPlayerSlotIntoCurioPacket.STREAM_CODEC, InsertPlayerSlotIntoCurioPacket::handle);
        registrar.playToServer(InsertGridEntryIntoCurioPacket.TYPE, InsertGridEntryIntoCurioPacket.STREAM_CODEC, InsertGridEntryIntoCurioPacket::handle);
        registrar.playToServer(InsertEquipmentStorageEntryIntoCurioPacket.TYPE, InsertEquipmentStorageEntryIntoCurioPacket.STREAM_CODEC, InsertEquipmentStorageEntryIntoCurioPacket::handle);
        registrar.playToServer(ExtractCurioToPlayerSlotPacket.TYPE, ExtractCurioToPlayerSlotPacket.STREAM_CODEC, ExtractCurioToPlayerSlotPacket::handle);
        registrar.playToServer(ExtractCurioToGridPacket.TYPE, ExtractCurioToGridPacket.STREAM_CODEC, ExtractCurioToGridPacket::handle);
        registrar.playToServer(ExtractCurioToEquipmentStoragePacket.TYPE, ExtractCurioToEquipmentStoragePacket.STREAM_CODEC, ExtractCurioToEquipmentStoragePacket::handle);
    }

    public static void syncMenu(Player player, GridInventoryMenu menu) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncGridInventoryPacket(menu.getGridData().copy()));
        }
    }

    public static void syncEquipmentStorage(Player player, net.minecraft.world.entity.EquipmentSlot slot, com.dreamingfish.gridinventory.common.data.EquipmentStorageData storage) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncEquipmentStoragePacket(slot, storage));
        }
    }
}
