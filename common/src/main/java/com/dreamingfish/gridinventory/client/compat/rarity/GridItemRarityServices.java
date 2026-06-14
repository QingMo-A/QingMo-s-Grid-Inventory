package com.dreamingfish.gridinventory.client.compat.rarity;

import java.util.Objects;

public final class GridItemRarityServices {
    private static GridItemRarityCompat compat = GridItemRarityCompat.NOOP;

    private GridItemRarityServices() {
    }

    public static void init(GridItemRarityCompat implementation) {
        compat = Objects.requireNonNull(implementation, "implementation");
    }

    public static GridItemRarityCompat compat() {
        return compat;
    }
}
