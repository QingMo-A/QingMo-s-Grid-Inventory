package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

public final class PlayerGridInventoryOpener {
    private PlayerGridInventoryOpener() {
    }

    public static void open(ServerPlayer player) {
        GridInventoryData data = PlayerPocketDefinitionManager.refreshShape(GridInventoryServices.playerData().getPlayerGridInventory(player)).copy();
        data.setChangeListener(() -> GridInventoryServices.playerData().setPlayerGridInventory(player, data.copy()));
        EquipmentStorageManager.initializeStorage(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
        EquipmentStorageManager.initializeStorage(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
        GridInventoryServices.playerData().setPlayerGridInventory(player, data.copy());
        GridInventoryServices.menus().openPlayerGridInventory(player, data.copy());
    }
}
