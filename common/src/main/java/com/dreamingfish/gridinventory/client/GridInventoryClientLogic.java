package com.dreamingfish.gridinventory.client;

import com.dreamingfish.gridinventory.client.pickup.ClientItemTargeting;
import com.dreamingfish.gridinventory.client.pickup.ClientPickupController;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class GridInventoryClientLogic {
    private GridInventoryClientLogic() {
    }

    public static void onClientTick() {
        ClientItemTargeting.tick();
        PickupPromptHud.tick();
        ClientPickupController.tick();
    }

    public static void addQuickEquipTooltip(Player player, ItemStack stack, List<Component> tooltip) {
        if (player != null && canQuickEquip(player, stack)) {
            tooltip.add(Component.translatable("tooltip.df_grid_inventory.right_click_equip"));
        }
    }

    public static boolean isFoldedRoll(ItemStack stack) {
        return GridBackpackItem.isFolded(stack) && GridBackpackItem.usesRollFoldedModel(stack);
    }

    private static boolean canQuickEquip(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).isEmpty() && EquipmentSlotHelper.canEquip(stack, slot, player)) {
                return true;
            }
        }
        return GridInventoryServices.accessories().canQuickEquip(player, stack);
    }
}
