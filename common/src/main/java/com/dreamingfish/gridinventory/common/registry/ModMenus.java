package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class ModMenus {
    public static Supplier<MenuType<GridInventoryMenu>> GRID_INVENTORY;

    private static boolean registered;

    private ModMenus() {
    }

    public static void register(GridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;

        GRID_INVENTORY = registry.registerMenu("grid_inventory", GridInventoryMenu::fromOpenData);
    }
}
