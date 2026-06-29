package com.dreamingfish.gridinventory.target.neoforge1211.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenuOpenData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;

public final class NeoForge1211SearchableGridContainerMenuOpenDataCodec {
    private NeoForge1211SearchableGridContainerMenuOpenDataCodec() {
    }

    public static void encode(SearchableGridContainerMenuOpenData data, RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos());
        data.playerGrid().encode(buf);
        data.containerGrid().encode(buf);
        buf.writeUtf(data.titleKey());
        buf.writeBoolean(data.creativeMode());
    }

    public static SearchableGridContainerMenuOpenData decode(RegistryFriendlyByteBuf buf) {
        BlockPos blockPos = buf.readBlockPos();
        GridInventoryData playerGrid = GridInventoryData.decode(buf);
        GridInventoryData data = GridInventoryData.decode(buf);
        String titleKey = buf.readUtf();
        boolean creativeMode = buf.readBoolean();
        return new SearchableGridContainerMenuOpenData(blockPos, playerGrid, data, titleKey, creativeMode);
    }
}
