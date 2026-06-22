package com.dreamingfish.gridinventory.client.creative;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record VanillaCreativeTabView(ResourceLocation id, Component title, ItemStack icon,
                                     List<ItemStack> displayItems, boolean searchTab, int sourceIndex) {
    public VanillaCreativeTabView {
        icon = icon.copy();
        displayItems = displayItems.stream().map(ItemStack::copy).toList();
    }
}
