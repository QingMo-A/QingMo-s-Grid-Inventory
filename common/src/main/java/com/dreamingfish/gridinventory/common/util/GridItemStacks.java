package com.dreamingfish.gridinventory.common.util;

import net.minecraft.world.item.ItemStack;

public final class GridItemStacks {
    private GridItemStacks() {
    }

    public static boolean sameItemSameData(ItemStack first, ItemStack second) {
        return ItemStack.matches(first.copyWithCount(1), second.copyWithCount(1));
    }
}
