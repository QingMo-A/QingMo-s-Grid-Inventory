package com.dreamingfish.gridinventory.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record SearchableGridContainerSpec(int columns, int rows, Component title, String containerType,
                                          double qualityMultiplier, ResourceLocation fallbackLootTableId) {
    public SearchableGridContainerSpec {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        containerType = containerType == null ? "" : containerType;
    }
}
