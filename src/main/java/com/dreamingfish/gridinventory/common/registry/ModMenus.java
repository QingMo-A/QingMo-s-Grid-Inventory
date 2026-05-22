package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DFGridInventoryMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GridInventoryMenu>> GRID_INVENTORY = MENUS.register(
            "grid_inventory",
            () -> new MenuType<>((IContainerFactory<GridInventoryMenu>) GridInventoryMenu::fromNetwork, FeatureFlags.VANILLA_SET)
    );

    private ModMenus() {
    }
}
