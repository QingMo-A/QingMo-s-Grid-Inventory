package com.dreamingfish.gridinventory.fabric.client;

import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;

public final class FabricClientScreens {
    private FabricClientScreens() {
    }

    public static void register() {
        MenuScreens.register(ModMenus.GRID_INVENTORY.get(), GridInventoryScreen::new);
    }
}
