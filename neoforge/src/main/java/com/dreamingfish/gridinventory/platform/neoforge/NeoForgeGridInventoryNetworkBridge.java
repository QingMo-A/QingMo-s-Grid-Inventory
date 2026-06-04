package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStoragePacket;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryPacket;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NeoForgeGridInventoryNetworkBridge implements GridInventoryNetworkBridge {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToAllPlayers(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void syncMenu(Player player, GridInventoryMenu menu) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendToPlayer(serverPlayer, new SyncGridInventoryPacket(menu.getGridData().copy()));
        }
    }

    @Override
    public void syncEquipmentStorage(Player player, EquipmentSlot slot, EquipmentStorageData storage) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendToPlayer(serverPlayer, new SyncEquipmentStoragePacket(slot, storage));
        }
    }
}
