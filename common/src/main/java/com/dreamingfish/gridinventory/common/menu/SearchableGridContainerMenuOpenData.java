package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.core.BlockPos;

public record SearchableGridContainerMenuOpenData(BlockPos blockPos, GridInventoryData playerGrid, GridInventoryData containerGrid,
                                                  String titleKey, boolean creativeMode) {
}
