package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static Supplier<CreativeModeTab> MAIN;

    private static boolean registered;

    private ModCreativeTabs() {
    }

    public static void register(GridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;

        MAIN = registry.registerCreativeTab("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.df_grid_inventory"))
                .icon(() -> new ItemStack(ModItems.SMALL_GRID_BAG.get()))
                .displayItems((parameters, output) -> {
                    output.accept(ModItems.SMALL_GRID_BAG.get());
                    output.accept(ModItems.GRID_BACKPACK.get());
                    output.accept(ModItems.GRAY_FIELD_BACKPACK.get());
                    output.accept(ModItems.LEATHER_BACKPACK.get());
                    output.accept(ModItems.LIME_HIKING_BACKPACK.get());
                    output.accept(ModItems.MEDIUM_HIKING_BACKPACK.get());
                    output.accept(ModItems.MILITARY_HIKING_BACKPACK.get());
                    output.accept(ModItems.TACTICAL_BACKPACK.get());
                })
                .build());
    }
}
