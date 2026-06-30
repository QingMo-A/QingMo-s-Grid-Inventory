package com.dreamingfish.gridinventory.common.loot;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record ContainerLootResult(boolean generated, List<ItemStack> generatedStacks, String source,
                                  Optional<String> manifestId) {
    public ContainerLootResult {
        generatedStacks = generatedStacks.stream().map(ItemStack::copy).toList();
        manifestId = manifestId == null ? Optional.empty() : manifestId;
    }
}
