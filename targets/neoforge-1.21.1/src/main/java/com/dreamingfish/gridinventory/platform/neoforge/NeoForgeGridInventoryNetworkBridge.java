package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryMessage;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.target.neoforge1211.protocol.NeoForge1211ProtocolCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class NeoForgeGridInventoryNetworkBridge implements GridInventoryNetworkBridge {
    @Override
    public void registerMessages() {
    }

    @Override
    public void sendToServer(GridMessage message) {
        NeoForge1211ProtocolCompat.sendToServer(message);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, GridMessage message) {
        NeoForge1211ProtocolCompat.sendToPlayer(player, message);
    }

    @Override
    public void sendToAllPlayers(GridMessage message) {
        NeoForge1211ProtocolCompat.sendToAllPlayers(message);
    }

    @Override
    public void syncMenu(Player player, GridInventoryMenu menu) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendToPlayer(serverPlayer, new SyncGridInventoryMessage(menu.getGridData().copy()));
        }
    }

    @Override
    public void syncEquipmentStorage(Player player, EquipmentSlot slot, EquipmentStorageData storage) {
        if (player instanceof ServerPlayer serverPlayer) {
            sendToPlayer(serverPlayer, new SyncEquipmentStorageMessage(slot, storage));
        }
    }
}
