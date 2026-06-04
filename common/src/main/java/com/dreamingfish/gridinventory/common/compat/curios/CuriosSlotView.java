package com.dreamingfish.gridinventory.common.compat.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public record CuriosSlotView(String identifier, int index, ItemStack stack, boolean active, ResourceLocation icon) {
}
