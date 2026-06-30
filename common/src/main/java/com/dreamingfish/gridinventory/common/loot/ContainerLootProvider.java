package com.dreamingfish.gridinventory.common.loot;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ContainerLootProvider {
    ContainerLootResult generateIntoGrid(ServerPlayer player, ServerLevel level, BlockPos pos,
                                         GridInventoryData grid, ContainerLootContext context);
}
