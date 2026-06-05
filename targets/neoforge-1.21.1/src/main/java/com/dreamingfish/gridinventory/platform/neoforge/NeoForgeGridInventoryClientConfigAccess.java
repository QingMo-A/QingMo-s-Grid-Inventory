package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;

public final class NeoForgeGridInventoryClientConfigAccess implements GridInventoryClientConfigAccess {
    @Override
    public String pickupKeyDefault() {
        return GridInventoryClientConfig.PICKUP_KEY_DEFAULT.get();
    }

    @Override
    public boolean highlightTargetItem() {
        return GridInventoryClientConfig.HIGHLIGHT_TARGET_ITEM.get();
    }

    @Override
    public String highlightColor() {
        return GridInventoryClientConfig.HIGHLIGHT_COLOR.get();
    }

    @Override
    public boolean showNearbyItemsPanel() {
        return GridInventoryClientConfig.SHOW_NEARBY_ITEMS_PANEL.get();
    }

    @Override
    public boolean enableSurvivalInventoryGridUi() {
        return GridInventoryClientConfig.ENABLE_SURVIVAL_INVENTORY_GRID_UI.get();
    }

    @Override
    public boolean showPlayerModel() {
        return GridInventoryClientConfig.SHOW_PLAYER_MODEL.get();
    }

    @Override
    public boolean showEquipmentPanel() {
        return GridInventoryClientConfig.SHOW_EQUIPMENT_PANEL.get();
    }

    @Override
    public boolean showGridColumn() {
        return GridInventoryClientConfig.SHOW_GRID_COLUMN.get();
    }

    @Override
    public String layoutMode() {
        return GridInventoryClientConfig.LAYOUT_MODE.get();
    }

    @Override
    public int columnGap() {
        return GridInventoryClientConfig.COLUMN_GAP.get();
    }

    @Override
    public boolean enableGridColumnScroll() {
        return GridInventoryClientConfig.ENABLE_GRID_COLUMN_SCROLL.get();
    }

    @Override
    public boolean enableNearbyColumnScroll() {
        return GridInventoryClientConfig.ENABLE_NEARBY_COLUMN_SCROLL.get();
    }

    @Override
    public int gridCellSize() {
        return GridInventoryClientConfig.GRID_CELL_SIZE.get();
    }

    @Override
    public int gridItemInnerPadding() {
        return GridInventoryClientConfig.GRID_ITEM_INNER_PADDING.get();
    }

    @Override
    public int equipmentFreeSlotSize() {
        return GridInventoryClientConfig.EQUIPMENT_FREE_SLOT_SIZE.get();
    }

    @Override
    public int hotbarFreeSlotSize() {
        return GridInventoryClientConfig.HOTBAR_FREE_SLOT_SIZE.get();
    }

    @Override
    public int freeSlotItemPadding() {
        return GridInventoryClientConfig.FREE_SLOT_ITEM_PADDING.get();
    }

    @Override
    public int nearbyPanelColumns() {
        return GridInventoryClientConfig.NEARBY_PANEL_COLUMNS.get();
    }

    @Override
    public int nearbyPanelVisibleRows() {
        return GridInventoryClientConfig.NEARBY_PANEL_VISIBLE_ROWS.get();
    }
}
