package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;

public interface GridInventoryPlatform {
    boolean isModLoaded(String modId);

    default boolean canEquip(ItemStack stack, EquipmentSlot slot, Player player) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot() == slot;
        }
        return slot == EquipmentSlot.CHEST && stack.getItem() instanceof ElytraItem;
    }

    GridInventoryNetworkBridge network();

    GridInventoryPlayerDataBridge playerData();

    GridInventoryAccessoryBridge accessories();

    GridInventoryItemStackDataBridge itemStackData();

    GridInventoryRegistryBridge registry();

    GridInventoryMenuBridge menus();

    GridInventoryConfigAccess config();

    GridInventoryClientConfigAccess clientConfig();
}
