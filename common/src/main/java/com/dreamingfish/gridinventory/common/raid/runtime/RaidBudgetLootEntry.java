package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.RaidLootItemDefinitionConfig;
import net.minecraft.world.item.ItemStack;

public record RaidBudgetLootEntry(
        ItemStack stack, int consumedValue, RaidLootItemDefinitionConfig definition) {
    public RaidBudgetLootEntry {
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        consumedValue = Math.max(0, consumedValue);
    }
}
