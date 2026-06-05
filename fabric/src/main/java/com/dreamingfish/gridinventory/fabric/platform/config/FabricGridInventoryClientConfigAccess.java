package com.dreamingfish.gridinventory.fabric.platform.config;

import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;

/**
 * TODO: Load Fabric values from config/df_grid_inventory_client.json or a future config UI.
 */
public final class FabricGridInventoryClientConfigAccess implements GridInventoryClientConfigAccess {
    @Override
    public String pickupKeyDefault() {
        return "R";
    }

    @Override
    public boolean highlightTargetItem() {
        return true;
    }

    @Override
    public String highlightColor() {
        return "FFFFFF";
    }

    @Override
    public boolean showNearbyItemsPanel() {
        return true;
    }

    @Override
    public boolean enableSurvivalInventoryGridUi() {
        return true;
    }

    @Override
    public boolean showPlayerModel() {
        return true;
    }

    @Override
    public boolean showEquipmentPanel() {
        return true;
    }

    @Override
    public boolean showGridColumn() {
        return true;
    }

    @Override
    public String layoutMode() {
        return "three_columns";
    }

    @Override
    public int columnGap() {
        return 8;
    }

    @Override
    public boolean enableGridColumnScroll() {
        return true;
    }

    @Override
    public boolean enableNearbyColumnScroll() {
        return true;
    }

    @Override
    public int gridCellSize() {
        return 27;
    }

    @Override
    public int gridItemInnerPadding() {
        return 2;
    }

    @Override
    public int equipmentFreeSlotSize() {
        return 32;
    }

    @Override
    public int hotbarFreeSlotSize() {
        return 21;
    }

    @Override
    public int freeSlotItemPadding() {
        return 3;
    }

    @Override
    public int nearbyPanelColumns() {
        return 6;
    }

    @Override
    public int nearbyPanelVisibleRows() {
        return 10;
    }
}
