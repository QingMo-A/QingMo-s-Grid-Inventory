package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;

public final class GridInventoryServices {
    private static GridInventoryPlatform platform;

    private GridInventoryServices() {
    }

    public static void init(GridInventoryPlatform platform) {
        if (platform == null) {
            throw new IllegalArgumentException("GridInventoryPlatform cannot be null.");
        }
        GridInventoryServices.platform = platform;
    }

    public static GridInventoryPlatform platform() {
        if (platform == null) {
            throw new IllegalStateException("GridInventoryServices has not been initialized. Call GridInventoryServices.init(...) from the platform entrypoint before using common services.");
        }
        return platform;
    }

    public static GridInventoryNetworkBridge network() {
        return platform().network();
    }

    public static GridInventoryPlayerDataBridge playerData() {
        return platform().playerData();
    }

    public static GridInventoryAccessoryBridge accessories() {
        return platform().accessories();
    }

    public static GridInventoryRegistryBridge registry() {
        return platform().registry();
    }

    public static GridInventoryMenuBridge menus() {
        return platform().menus();
    }

    public static GridInventoryConfigAccess config() {
        return platform().config();
    }

    public static GridInventoryClientConfigAccess clientConfig() {
        return platform().clientConfig();
    }
}
