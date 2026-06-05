package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public final class ModMenus {
    public static final Supplier<MenuType<GridInventoryMenu>> GRID_INVENTORY = GridInventoryServices.registry().registerMenu("grid_inventory", GridInventoryMenu::fromOpenData);

    private ModMenus() {
    }

    public static void bootstrap() {
    }
}
