package com.dreamingfish.gridinventory.common.loot;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.raid.config.RaidContainerTypeConfig;
import com.dreamingfish.gridinventory.common.raid.config.RaidLootItemDefinitionRegistry;
import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfigRegistry;
import com.dreamingfish.gridinventory.common.raid.runtime.RaidBudgetLootContext;
import com.dreamingfish.gridinventory.common.raid.runtime.RaidBudgetLootGenerator;
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
        if (context.raidId() > 0L && context.pointBudget() > 0 && !context.mapId().isBlank()
                && !context.containerType().isBlank()) {
            Optional<RaidContainerTypeConfig> type = RaidMapConfigRegistry.get(context.mapId())
                    .flatMap(map -> map.containerTypes().stream()
                            .filter(candidate -> candidate.id().equals(context.containerType())).findFirst());
            if (type.isPresent()) {
                int maxStacks = enabledCells(grid);
                RaidBudgetLootContext budgetContext = new RaidBudgetLootContext(
                        context.mapId(), context.containerType(), context.pointBudget(),
                        context.qualityMultiplier(), context.lootSeed(), maxStacks, type.get().allowedCategories(),
                        RaidLootItemDefinitionRegistry.enabled());
                try {
                    var generated = RaidBudgetLootGenerator.generate(budgetContext);
                    List<ItemStack> stacks = generated.entries().stream()
                            .map(entry -> entry.stack().copy()).toList();
                    int inserted = GridLootFiller.insertAll(grid, stacks);
                    if (generated.generatedAny() && inserted > 0) {
                        DFGridInventory.LOGGER.debug(
                                "Raid budget loot generated mapId={} containerType={} pointBudget={} qualityMultiplier={} effectiveBudget={} consumedBudget={} entries={} candidates={}",
                                context.mapId(), context.containerType(), context.pointBudget(),
                                context.qualityMultiplier(), budgetContext.effectiveBudget(),
                                generated.consumedBudget(), generated.entries().size(),
                                generated.candidateCount());
                        return new ContainerLootResult(true, stacks, "raid_budget", Optional.empty());
                    }
                    DFGridInventory.LOGGER.debug(
                            "Raid budget loot skipped mapId={} containerType={} reason=no_inserted_entries",
                            context.mapId(), context.containerType());
                } catch (RuntimeException exception) {
                    DFGridInventory.LOGGER.warn(
                            "Raid budget loot failed; using fallback mapId={} containerType={}",
                            context.mapId(), context.containerType(), exception);
                }
            }
        }
        return GridInventoryServices.containerLootProvider()
                .generateIntoGrid(player, level, pos, grid, context);
    }

    private static int enabledCells(GridInventoryData grid) {
        int cells = 0;
        for (int y = 0; y < grid.getRows(); y++) {
            for (int x = 0; x < grid.getColumns(); x++) {
                if (grid.isEnabledCell(x, y)) cells++;
            }
        }
        return cells;
    }
}
