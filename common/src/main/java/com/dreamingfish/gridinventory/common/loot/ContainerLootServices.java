package com.dreamingfish.gridinventory.common.loot;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class ContainerLootServices {
    private static RaidContainerLootResolver raidResolver = new NoopRaidContainerLootResolver();

    private ContainerLootServices() {
    }

    public static void setRaidResolver(RaidContainerLootResolver resolver) {
        raidResolver = resolver == null ? new NoopRaidContainerLootResolver() : resolver;
    }

    public static ContainerLootResult generateIntoGrid(ServerPlayer player, ServerLevel level, BlockPos pos,
                                                       GridInventoryData grid, ContainerLootContext context) {
        Optional<ContainerLootManifestRef> ref = raidResolver.resolveManifestRef(level, pos);
        if (ref.isPresent()) {
            Optional<List<ItemStack>> loot = raidResolver.resolveManifestLoot(ref.get());
            if (loot.isPresent()) {
                GridLootFiller.insertAll(grid, loot.get());
                return new ContainerLootResult(true, loot.get(), "raid_manifest",
                        Optional.of(ref.get().raidId() + ":" + ref.get().anchorId()));
            }
        }
        return GridInventoryServices.containerLootProvider()
                .generateIntoGrid(player, level, pos, grid, context);
    }
}
