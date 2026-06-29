package com.dreamingfish.gridinventory;

import com.dreamingfish.gridinventory.common.registry.ModCreativeTabs;
import com.dreamingfish.gridinventory.common.registry.ModBlocks;
import com.dreamingfish.gridinventory.common.registry.ModBlockEntities;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;

import java.util.Objects;

public final class DFGridInventoryCommon {
    private DFGridInventoryCommon() {
    }

    public static void registerContent(GridInventoryRegistryBridge registry) {
        Objects.requireNonNull(registry, "registry");
        ModBlocks.register(registry);
        ModItems.register(registry);
        ModBlockEntities.register(registry);
        ModMenus.register(registry);
        ModCreativeTabs.register(registry);
    }
}
