package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;

public interface GridInventoryPlatform {
    boolean isModLoaded(String modId);

    GridInventoryNetworkBridge network();

    GridInventoryPlayerDataBridge playerData();

    GridInventoryAccessoryBridge accessories();

    GridInventoryRegistryBridge registry();

    GridInventoryConfigAccess config();

    GridInventoryClientConfigAccess clientConfig();
}
