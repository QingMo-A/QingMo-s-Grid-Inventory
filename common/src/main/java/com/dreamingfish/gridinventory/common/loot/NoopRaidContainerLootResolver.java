package com.dreamingfish.gridinventory.common.loot;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class NoopRaidContainerLootResolver implements RaidContainerLootResolver {
    @Override
    public Optional<ContainerLootManifestRef> resolveManifestRef(ServerLevel level, BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Optional<List<ItemStack>> resolveManifestLoot(ContainerLootManifestRef ref) {
        return Optional.empty();
    }
}
