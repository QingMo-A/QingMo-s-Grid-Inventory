package com.dreamingfish.gridinventory.fabric.platform.network;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStoragePacket;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryPacket;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class FabricGridInventoryNetworkBridge implements GridInventoryNetworkBridge {
    private static MinecraftServer currentServer;

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToAllPlayers(CustomPacketPayload payload) {
        if (currentServer == null) {
            return;
        }
        for (ServerPlayer player : PlayerLookup.all(currentServer)) {
            sendToPlayer(player, payload);
        }
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
