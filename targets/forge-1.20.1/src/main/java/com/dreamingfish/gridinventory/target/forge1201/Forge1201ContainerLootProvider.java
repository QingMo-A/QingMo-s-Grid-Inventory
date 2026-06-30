package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.loot.ContainerLootContext;
import com.dreamingfish.gridinventory.common.loot.ContainerLootProvider;
import com.dreamingfish.gridinventory.common.loot.ContainerLootResult;
import com.dreamingfish.gridinventory.common.loot.GridLootFiller;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class Forge1201ContainerLootProvider implements ContainerLootProvider {
    @Override
    public ContainerLootResult generateIntoGrid(ServerPlayer player, ServerLevel level, BlockPos pos,
                                                GridInventoryData grid, ContainerLootContext context) {
        if (context.fallbackLootTableId() == null) {
            return new ContainerLootResult(false, List.of(), "none", Optional.empty());
        }
        LootTable table = player.server.getLootData().getLootTable(context.fallbackLootTableId());
        if (table == LootTable.EMPTY) {
            DFGridInventory.LOGGER.warn("Missing fallback loot table {}", context.fallbackLootTableId());
            return new ContainerLootResult(false, List.of(), "missing_loot_table", Optional.empty());
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withLuck(player.getLuck())
                .create(LootContextParamSets.CHEST);
        List<ItemStack> stacks = table.getRandomItems(params);
        GridLootFiller.insertAll(grid, stacks);
        return new ContainerLootResult(true, stacks, "fallback_loot_table", Optional.empty());
    }
}
