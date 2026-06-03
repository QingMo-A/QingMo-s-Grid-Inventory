package com.dreamingfish.gridinventory.common.item;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingManager;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class GridBackpackItem extends Item implements ICurioItem {
    public GridBackpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "back".equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
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
        if (stack.has(ModDataComponents.BACKPACK_FOLDED.get())) {
            stack.set(ModDataComponents.BACKPACK_FOLDED.get(), false);
        }
    }
}
