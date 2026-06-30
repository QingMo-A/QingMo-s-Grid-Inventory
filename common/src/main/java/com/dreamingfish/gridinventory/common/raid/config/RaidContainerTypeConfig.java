package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record RaidContainerTypeConfig(String id, ResourceLocation block, int columns, int rows,
                                      ResourceLocation fallbackLootTable, double defaultQualityMultiplier,
                                      List<String> allowedCategories) {
}
