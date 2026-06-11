package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryMessage;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201ProtocolCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class Forge1201NetworkBridge implements GridInventoryNetworkBridge {
    @Override
    public void registerMessages() {
        Forge1201ProtocolCompat.register();
    }

    @Override
    public void sendToServer(GridMessage message) {
        Forge1201ProtocolCompat.sendToServer(message);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, GridMessage message) {
        Forge1201ProtocolCompat.sendToPlayer(player, message);
    }

    @Override
    public void sendToAllPlayers(GridMessage message) {
        Forge1201ProtocolCompat.sendToAllPlayers(message);
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
