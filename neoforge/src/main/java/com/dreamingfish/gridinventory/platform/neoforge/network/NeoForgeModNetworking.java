package com.dreamingfish.gridinventory.platform.neoforge.network;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.*;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;
import com.dreamingfish.gridinventory.platform.neoforge.NeoForgeGridPacketContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;

public final class NeoForgeModNetworking {
    private NeoForgeModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncItemSizeRulesPacket.TYPE, SyncItemSizeRulesPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, SyncItemSizeRulesPacket::handle));
        registrar.playToClient(SyncBackpackFoldingRulesPacket.TYPE, SyncBackpackFoldingRulesPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, SyncBackpackFoldingRulesPacket::handle));
        registrar.playToClient(SyncGridInventoryPacket.TYPE, SyncGridInventoryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, SyncGridInventoryPacket::handle));
        registrar.playToClient(SyncEquipmentStoragePacket.TYPE, SyncEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> handle(packet, context, SyncEquipmentStoragePacket::handle));
        registrar.playToServer(MoveGridEntryPacket.TYPE, MoveGridEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, MoveGridEntryPacket::handle));
        registrar.playToServer(ToggleGridEntryBackpackFoldPacket.TYPE, ToggleGridEntryBackpackFoldPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ToggleGridEntryBackpackFoldPacket::handle));
        registrar.playToServer(ToggleEquipmentStorageEntryBackpackFoldPacket.TYPE, ToggleEquipmentStorageEntryBackpackFoldPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ToggleEquipmentStorageEntryBackpackFoldPacket::handle));
        registrar.playToServer(InsertFromPlayerInventoryPacket.TYPE, InsertFromPlayerInventoryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, InsertFromPlayerInventoryPacket::handle));
        registrar.playToServer(ExtractToPlayerInventoryPacket.TYPE, ExtractToPlayerInventoryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ExtractToPlayerInventoryPacket::handle));
        registrar.playToServer(ExtractGridEntryToPlayerSlotPacket.TYPE, ExtractGridEntryToPlayerSlotPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ExtractGridEntryToPlayerSlotPacket::handle));
        registrar.playToServer(OpenPlayerGridInventoryPacket.TYPE, OpenPlayerGridInventoryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, OpenPlayerGridInventoryPacket::handle));
        registrar.playToServer(ManualPickupItemPacket.TYPE, ManualPickupItemPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ManualPickupItemPacket::handle));
        registrar.playToServer(PickupGroundItemIntoGridPacket.TYPE, PickupGroundItemIntoGridPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, PickupGroundItemIntoGridPacket::handle));
        registrar.playToServer(PickupGroundItemIntoEquipmentStoragePacket.TYPE, PickupGroundItemIntoEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> handle(packet, context, PickupGroundItemIntoEquipmentStoragePacket::handle));
        registrar.playToServer(InsertIntoEquipmentStoragePacket.TYPE, InsertIntoEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> handle(packet, context, InsertIntoEquipmentStoragePacket::handle));
        registrar.playToServer(MoveEquipmentStorageEntryPacket.TYPE, MoveEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, MoveEquipmentStorageEntryPacket::handle));
        registrar.playToServer(ExtractEquipmentStorageEntryPacket.TYPE, ExtractEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ExtractEquipmentStorageEntryPacket::handle));
        registrar.playToServer(TransferGridEntryIntoEquipmentStoragePacket.TYPE, TransferGridEntryIntoEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> handle(packet, context, TransferGridEntryIntoEquipmentStoragePacket::handle));
        registrar.playToServer(TransferEquipmentStorageEntryIntoGridPacket.TYPE, TransferEquipmentStorageEntryIntoGridPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, TransferEquipmentStorageEntryIntoGridPacket::handle));
        registrar.playToServer(TransferEquipmentStorageEntryPacket.TYPE, TransferEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, TransferEquipmentStorageEntryPacket::handle));
        registrar.playToServer(MovePlayerFreeSlotPacket.TYPE, MovePlayerFreeSlotPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, MovePlayerFreeSlotPacket::handle));
        registrar.playToServer(QuickEquipGridEntryPacket.TYPE, QuickEquipGridEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, QuickEquipGridEntryPacket::handle));
        registrar.playToServer(QuickEquipEquipmentStorageEntryPacket.TYPE, QuickEquipEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, QuickEquipEquipmentStorageEntryPacket::handle));
        registrar.playToServer(QuickEquipPlayerSlotPacket.TYPE, QuickEquipPlayerSlotPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, QuickEquipPlayerSlotPacket::handle));
        registrar.playToServer(DropGridEntryPacket.TYPE, DropGridEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, DropGridEntryPacket::handle));
        registrar.playToServer(DropEquipmentStorageEntryPacket.TYPE, DropEquipmentStorageEntryPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, DropEquipmentStorageEntryPacket::handle));
        registrar.playToServer(InsertPlayerSlotIntoCurioPacket.TYPE, InsertPlayerSlotIntoCurioPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, InsertPlayerSlotIntoCurioPacket::handle));
        registrar.playToServer(InsertGridEntryIntoCurioPacket.TYPE, InsertGridEntryIntoCurioPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, InsertGridEntryIntoCurioPacket::handle));
        registrar.playToServer(InsertEquipmentStorageEntryIntoCurioPacket.TYPE, InsertEquipmentStorageEntryIntoCurioPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, InsertEquipmentStorageEntryIntoCurioPacket::handle));
        registrar.playToServer(ExtractCurioToPlayerSlotPacket.TYPE, ExtractCurioToPlayerSlotPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ExtractCurioToPlayerSlotPacket::handle));
        registrar.playToServer(ExtractCurioToGridPacket.TYPE, ExtractCurioToGridPacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ExtractCurioToGridPacket::handle));
        registrar.playToServer(ExtractCurioToEquipmentStoragePacket.TYPE, ExtractCurioToEquipmentStoragePacket.STREAM_CODEC, (packet, context) -> handle(packet, context, ExtractCurioToEquipmentStoragePacket::handle));
    }

    private static <T extends CustomPacketPayload> void handle(T packet, IPayloadContext context, BiConsumer<T, GridPacketContext> handler) {
        handler.accept(packet, new NeoForgeGridPacketContext(context));
    }

    public static void syncMenu(Player player, GridInventoryMenu menu) {
        GridInventoryServices.network().syncMenu(player, menu);
    }

    public static void syncEquipmentStorage(Player player, net.minecraft.world.entity.EquipmentSlot slot, com.dreamingfish.gridinventory.common.data.EquipmentStorageData storage) {
        GridInventoryServices.network().syncEquipmentStorage(player, slot, storage);
    }
}
