package com.dreamingfish.gridinventory.target.forge1201.menu;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import net.minecraft.network.FriendlyByteBuf;

public final class Forge1201MenuOpenDataCodec {
    private Forge1201MenuOpenDataCodec() {
    }

    public static void encode(GridInventoryMenuOpenData data, FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Forge 1.20.1 menu open data codec is blocked until GridInventoryData has a 1.20.1-compatible buffer codec");
    }

    public static GridInventoryMenuOpenData decode(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Forge 1.20.1 menu open data codec is blocked until GridInventoryData has a 1.20.1-compatible buffer codec");
    }
}
