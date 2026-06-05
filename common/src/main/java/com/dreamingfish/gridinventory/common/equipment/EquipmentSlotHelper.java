package com.dreamingfish.gridinventory.common.equipment;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class EquipmentSlotHelper {
    private EquipmentSlotHelper() {
    }

    public static boolean canEquip(ItemStack stack, EquipmentSlot slot, Player player) {
        return GridInventoryServices.platform().canEquip(stack, slot, player);
    }
}
