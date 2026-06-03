package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DFGridInventoryMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
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

    private ModCreativeTabs() {
    }
}
