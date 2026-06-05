package com.dreamingfish.gridinventory.fabric.client;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.client.GridInventoryClientLogic;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.resources.ResourceLocation;

public final class FabricItemProperties {
    private FabricItemProperties() {
    }

    @SuppressWarnings("deprecation")
    public static void register() {
        ResourceLocation folded = ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "folded");
        ResourceLocation foldedRoll = ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "folded_roll");
        FabricModelPredicateProviderRegistry.register(ModItems.GRAY_FIELD_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.LEATHER_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.LEATHER_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> GridInventoryClientLogic.isFoldedRoll(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.LIME_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.LIME_HIKING_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> GridInventoryClientLogic.isFoldedRoll(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.MEDIUM_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.MEDIUM_HIKING_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> GridInventoryClientLogic.isFoldedRoll(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.MILITARY_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        FabricModelPredicateProviderRegistry.register(ModItems.TACTICAL_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
    }
}
