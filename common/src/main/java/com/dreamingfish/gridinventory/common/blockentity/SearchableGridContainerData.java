package com.dreamingfish.gridinventory.common.blockentity;

import com.dreamingfish.gridinventory.common.block.BasicSearchableGridContainerBlock;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.level.block.state.BlockState;

public final class SearchableGridContainerData {
    public static final String GRID_DATA_KEY = "GridData";
    public static final String LOOT_GENERATED_KEY = "LootGenerated";

    private GridInventoryData gridData;
    private boolean lootGenerated;
    private final Runnable changeListener;

    public SearchableGridContainerData(BlockState state, Runnable changeListener) {
        this.gridData = createDefaultGrid(state);
        this.changeListener = changeListener;
        attachListener();
    }

    public GridInventoryData getGridData() {
        return gridData;
    }

    public void setGridData(GridInventoryData gridData) {
        this.gridData = gridData == null ? new GridInventoryData(8, 5) : gridData;
        attachListener();
        changeListener.run();
    }

    public boolean isLootGenerated() {
        return lootGenerated;
    }

    public void setLootGenerated(boolean lootGenerated) {
        this.lootGenerated = lootGenerated;
        changeListener.run();
    }

    public void load(CompoundTag tag, BlockState state, DynamicOps<Tag> ops) {
        if (tag.contains(GRID_DATA_KEY)) {
            gridData = GridInventoryData.CODEC.parse(ops, tag.get(GRID_DATA_KEY))
                    .result()
                    .orElseGet(() -> createDefaultGrid(state));
        } else {
            gridData = createDefaultGrid(state);
        }
        lootGenerated = tag.getBoolean(LOOT_GENERATED_KEY);
        attachListener();
    }

    public void save(CompoundTag tag, DynamicOps<Tag> ops) {
        DataResult<Tag> encoded = GridInventoryData.CODEC.encodeStart(ops, gridData);
        encoded.result().ifPresent(value -> tag.put(GRID_DATA_KEY, value));
        tag.putBoolean(LOOT_GENERATED_KEY, lootGenerated);
    }

    private void attachListener() {
        gridData.setChangeListener(changeListener);
    }

    private static GridInventoryData createDefaultGrid(BlockState state) {
        if (state.getBlock() instanceof BasicSearchableGridContainerBlock block) {
            return new GridInventoryData(block.columns(), block.rows());
        }
        return new GridInventoryData(8, 5);
    }
}
