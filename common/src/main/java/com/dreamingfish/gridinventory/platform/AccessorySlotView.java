package com.dreamingfish.gridinventory.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record AccessorySlotView(String identifier, int index, ItemStack stack, boolean active, ResourceLocation icon) {
}
