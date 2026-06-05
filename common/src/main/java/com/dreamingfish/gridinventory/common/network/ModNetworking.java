package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void syncMenu(Player player, GridInventoryMenu menu) {
        GridInventoryServices.network().syncMenu(player, menu);
    }

    public static void syncEquipmentStorage(Player player, EquipmentSlot slot, EquipmentStorageData storage) {
        GridInventoryServices.network().syncEquipmentStorage(player, slot, storage);
    }
}
