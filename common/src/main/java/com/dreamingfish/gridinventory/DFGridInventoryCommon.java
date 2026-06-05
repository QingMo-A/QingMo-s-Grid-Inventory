package com.dreamingfish.gridinventory;

import com.dreamingfish.gridinventory.common.registry.ModCreativeTabs;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;

import java.util.Objects;

public final class DFGridInventoryCommon {
    private DFGridInventoryCommon() {
    }

    public static void registerContent(GridInventoryRegistryBridge registry) {
        Objects.requireNonNull(registry, "registry");
        ModItems.bootstrap();
        ModMenus.bootstrap();
        ModDataComponents.bootstrap();
        ModCreativeTabs.bootstrap();
    }
}
