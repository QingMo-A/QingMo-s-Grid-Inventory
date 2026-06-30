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
    private static final String ANCHOR_ID_KEY = "AnchorId";
    private static final String ZONE_ID_KEY = "ZoneId";
    private static final String CONTAINER_TYPE_KEY = "ContainerType";
    private static final String RAID_ID_KEY = "RaidId";
    private static final String LOOT_SEED_KEY = "LootSeed";
    private static final String POINT_BUDGET_KEY = "PointBudget";

    private GridInventoryData gridData;
    private boolean lootGenerated;
    private String anchorId = "";
    private String zoneId = "";
    private String containerType = "";
    private long raidId;
    private long lootSeed;
    private int pointBudget;
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

    public String getAnchorId() { return anchorId; }
    public String getZoneId() { return zoneId; }
    public String getContainerType() { return containerType; }
    public long getRaidId() { return raidId; }
    public long getLootSeed() { return lootSeed; }
    public int getPointBudget() { return pointBudget; }

    public void setLootMetadata(String anchorId, String zoneId, String containerType, long raidId,
                                long lootSeed, int pointBudget) {
        this.anchorId = anchorId == null ? "" : anchorId;
        this.zoneId = zoneId == null ? "" : zoneId;
        this.containerType = containerType == null ? "" : containerType;
        this.raidId = raidId;
        this.lootSeed = lootSeed;
        this.pointBudget = pointBudget;
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
        anchorId = tag.getString(ANCHOR_ID_KEY);
        zoneId = tag.getString(ZONE_ID_KEY);
        containerType = tag.getString(CONTAINER_TYPE_KEY);
        raidId = tag.getLong(RAID_ID_KEY);
        lootSeed = tag.getLong(LOOT_SEED_KEY);
        pointBudget = tag.getInt(POINT_BUDGET_KEY);
        attachListener();
    }

    public void save(CompoundTag tag, DynamicOps<Tag> ops) {
        DataResult<Tag> encoded = GridInventoryData.CODEC.encodeStart(ops, gridData);
        encoded.result().ifPresent(value -> tag.put(GRID_DATA_KEY, value));
        tag.putBoolean(LOOT_GENERATED_KEY, lootGenerated);
        tag.putString(ANCHOR_ID_KEY, anchorId);
        tag.putString(ZONE_ID_KEY, zoneId);
        tag.putString(CONTAINER_TYPE_KEY, containerType);
        tag.putLong(RAID_ID_KEY, raidId);
        tag.putLong(LOOT_SEED_KEY, lootSeed);
        tag.putInt(POINT_BUDGET_KEY, pointBudget);
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
