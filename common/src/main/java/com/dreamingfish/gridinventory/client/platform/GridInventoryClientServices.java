package com.dreamingfish.gridinventory.client.platform;

import java.util.Objects;

public final class GridInventoryClientServices {
    private static GridInventoryClientBridge bridge = GridInventoryClientBridge.NOOP;

    private GridInventoryClientServices() {
    }

    public static void init(GridInventoryClientBridge implementation) {
        bridge = Objects.requireNonNull(implementation, "implementation");
    }

    public static GridInventoryClientBridge bridge() {
        return bridge;
    }
}
