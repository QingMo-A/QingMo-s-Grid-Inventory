package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.OpenPlayerGridInventoryMessage;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryMessage;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public interface GridInventoryNetworkBridge {
    void registerMessages();

    void sendToServer(GridMessage message);

    void sendToPlayer(ServerPlayer player, GridMessage message);

    void sendToAllPlayers(GridMessage message);

    void syncMenu(Player player, GridInventoryMenu menu);

    void syncEquipmentStorage(Player player, EquipmentSlot slot, EquipmentStorageData storage);

    default void requestOpenPlayerGridInventory() {
        sendToServer(OpenPlayerGridInventoryMessage.INSTANCE);
    }

    default void syncGridInventory(ServerPlayer player, GridInventoryData data) {
        sendToPlayer(player, new SyncGridInventoryMessage(data.copy()));
    }

    default void syncEquipmentStorage(ServerPlayer player, EquipmentSlot slot, EquipmentStorageData storage) {
        sendToPlayer(player, new SyncEquipmentStorageMessage(slot, storage));
    }
}
