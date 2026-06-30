package com.dreamingfish.gridinventory.common.loot;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface RaidContainerLootResolver {
    Optional<ContainerLootManifestRef> resolveManifestRef(ServerLevel level, BlockPos pos);

    Optional<List<ItemStack>> resolveManifestLoot(ContainerLootManifestRef ref);
}
