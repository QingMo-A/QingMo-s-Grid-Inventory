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
        registrar.playToServer(MoveGridEntryPacket.TYPE, MoveGridEntryPacket.STREAM_CODEC, MoveGridEntryPacket::handle);
        registrar.playToServer(InsertFromPlayerInventoryPacket.TYPE, InsertFromPlayerInventoryPacket.STREAM_CODEC, InsertFromPlayerInventoryPacket::handle);
        registrar.playToServer(ExtractToPlayerInventoryPacket.TYPE, ExtractToPlayerInventoryPacket.STREAM_CODEC, ExtractToPlayerInventoryPacket::handle);
        registrar.playToServer(ExtractGridEntryToPlayerSlotPacket.TYPE, ExtractGridEntryToPlayerSlotPacket.STREAM_CODEC, ExtractGridEntryToPlayerSlotPacket::handle);
        registrar.playToServer(OpenPlayerGridInventoryPacket.TYPE, OpenPlayerGridInventoryPacket.STREAM_CODEC, OpenPlayerGridInventoryPacket::handle);
        registrar.playToServer(ManualPickupItemPacket.TYPE, ManualPickupItemPacket.STREAM_CODEC, ManualPickupItemPacket::handle);
        registrar.playToServer(PickupGroundItemIntoGridPacket.TYPE, PickupGroundItemIntoGridPacket.STREAM_CODEC, PickupGroundItemIntoGridPacket::handle);
        registrar.playToServer(InsertIntoEquipmentStoragePacket.TYPE, InsertIntoEquipmentStoragePacket.STREAM_CODEC, InsertIntoEquipmentStoragePacket::handle);
        registrar.playToServer(MoveEquipmentStorageEntryPacket.TYPE, MoveEquipmentStorageEntryPacket.STREAM_CODEC, MoveEquipmentStorageEntryPacket::handle);
        registrar.playToServer(ExtractEquipmentStorageEntryPacket.TYPE, ExtractEquipmentStorageEntryPacket.STREAM_CODEC, ExtractEquipmentStorageEntryPacket::handle);
        registrar.playToServer(TransferGridEntryIntoEquipmentStoragePacket.TYPE, TransferGridEntryIntoEquipmentStoragePacket.STREAM_CODEC, TransferGridEntryIntoEquipmentStoragePacket::handle);
        registrar.playToServer(TransferEquipmentStorageEntryIntoGridPacket.TYPE, TransferEquipmentStorageEntryIntoGridPacket.STREAM_CODEC, TransferEquipmentStorageEntryIntoGridPacket::handle);
        registrar.playToServer(TransferEquipmentStorageEntryPacket.TYPE, TransferEquipmentStorageEntryPacket.STREAM_CODEC, TransferEquipmentStorageEntryPacket::handle);
        registrar.playToServer(MovePlayerFreeSlotPacket.TYPE, MovePlayerFreeSlotPacket.STREAM_CODEC, MovePlayerFreeSlotPacket::handle);
        registrar.playToServer(QuickEquipGridEntryPacket.TYPE, QuickEquipGridEntryPacket.STREAM_CODEC, QuickEquipGridEntryPacket::handle);
        registrar.playToServer(QuickEquipEquipmentStorageEntryPacket.TYPE, QuickEquipEquipmentStorageEntryPacket.STREAM_CODEC, QuickEquipEquipmentStorageEntryPacket::handle);
    }

    public static void syncMenu(Player player, GridInventoryMenu menu) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncGridInventoryPacket(menu.getGridData().copy()));
        }
    }
}
