package com.dreamingfish.gridinventory.target.forge1201.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenuOpenData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public final class Forge1201SearchableGridContainerMenuOpenDataCodec {
    private Forge1201SearchableGridContainerMenuOpenDataCodec() {
    }

    public static void encode(SearchableGridContainerMenuOpenData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos());
        data.containerGrid().encode(buf);
        buf.writeUtf(data.titleKey());
        buf.writeBoolean(data.creativeMode());
    }

    public static SearchableGridContainerMenuOpenData decode(FriendlyByteBuf buf) {
        BlockPos blockPos = buf.readBlockPos();
        GridInventoryData data = GridInventoryData.decode(buf);
        String titleKey = buf.readUtf();
        boolean creativeMode = buf.readBoolean();
        return new SearchableGridContainerMenuOpenData(blockPos, data, titleKey, creativeMode);
    }
}
