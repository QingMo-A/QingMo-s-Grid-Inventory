package com.dreamingfish.gridinventory.client.creative;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface VanillaCreativeTabProvider {
    VanillaCreativeTabProvider EMPTY = new VanillaCreativeTabProvider() {
    };

    default List<VanillaCreativeTabView> tabs() {
        return List.of();
    }

    default List<ItemStack> search(String query) {
        return List.of();
    }

    default Optional<VanillaCreativeTabView> selectedDefaultTab() {
        return tabs().stream().findFirst();
    }

    default void refresh() {
    }
}
