package com.dreamingfish.gridinventory.common.blockentity;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SearchableGridContainerBlockEntity extends BlockEntity {
    private final SearchableGridContainerData data;

    public SearchableGridContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEARCHABLE_GRID_CONTAINER.get(), pos, state);
        data = new SearchableGridContainerData(state, this::setChanged);
    }

    public GridInventoryData getGridData() {
        return data.getGridData();
    }

    public void setGridData(GridInventoryData gridData) {
        data.setGridData(gridData);
    }

    public boolean isLootGenerated() {
        return data.isLootGenerated();
    }

    public void setLootGenerated(boolean lootGenerated) {
        data.setLootGenerated(lootGenerated);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        data.load(tag, getBlockState(), NbtOps.INSTANCE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        data.save(tag, NbtOps.INSTANCE);
    }
}
