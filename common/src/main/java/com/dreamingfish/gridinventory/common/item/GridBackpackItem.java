package com.dreamingfish.gridinventory.common.item;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingManager;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GridBackpackItem extends Item {
    public GridBackpackItem(Properties properties) {
        super(properties);
    }

    public static boolean isFolded(ItemStack stack) {
        if (!(stack.getItem() instanceof GridBackpackItem)) {
            return false;
        }
        Boolean folded = stack.get(ModDataComponents.BACKPACK_FOLDED.get());
        return folded == null || folded;
    }

    public static boolean canFold(ItemStack stack) {
        EquipmentStorageData storage = stack.get(ModDataComponents.EQUIPMENT_STORAGE.get());
        return storage == null || storage.containers().stream()
                .allMatch(container -> container.inventory().getEntries().isEmpty());
    }

    public static int foldedWidth(ItemStack stack) {
        return BackpackFoldingManager.foldedWidth(stack);
    }

    public static int foldedHeight(ItemStack stack) {
        return BackpackFoldingManager.foldedHeight(stack);
    }

    public static boolean usesRollFoldedModel(ItemStack stack) {
        return BackpackFoldingManager.usesRollModel(stack);
    }

    public static boolean toggleFolded(ItemStack stack) {
        if (!(stack.getItem() instanceof GridBackpackItem)) {
            return false;
        }
        if (isFolded(stack)) {
            unfold(stack);
            return true;
        }
        if (!canFold(stack)) {
            return false;
        }
        stack.set(ModDataComponents.BACKPACK_FOLDED.get(), true);
        return true;
    }

    public static void unfold(ItemStack stack) {
        if (stack.getItem() instanceof GridBackpackItem) {
            stack.set(ModDataComponents.BACKPACK_FOLDED.get(), false);
        }
    }
}
