package com.dreamingfish.gridinventory.client.screen.panel;

import net.minecraft.world.item.ItemStack;

public record CreativeItemReference(int tabIndex, int itemIndex, ItemStack stack) {
    public CreativeItemReference {
        stack = stack.copy();
    }
}
