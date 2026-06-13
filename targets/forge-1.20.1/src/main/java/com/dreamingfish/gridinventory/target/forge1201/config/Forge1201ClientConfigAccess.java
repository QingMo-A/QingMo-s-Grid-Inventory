package com.dreamingfish.gridinventory.target.forge1201.config;

import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;

public final class Forge1201ClientConfigAccess implements GridInventoryClientConfigAccess {
    @Override public String pickupKeyDefault() { return Forge1201ClientConfig.PICKUP_KEY_DEFAULT.get(); }
    @Override public boolean highlightTargetItem() { return Forge1201ClientConfig.HIGHLIGHT_TARGET_ITEM.get(); }
    @Override public String highlightColor() { return Forge1201ClientConfig.HIGHLIGHT_COLOR.get(); }
    @Override public boolean showNearbyItemsPanel() { return Forge1201ClientConfig.SHOW_NEARBY_ITEMS_PANEL.get(); }
    @Override public boolean enableSurvivalInventoryGridUi() { return Forge1201ClientConfig.ENABLE_SURVIVAL_INVENTORY_GRID_UI.get(); }
    @Override public boolean showPlayerModel() { return Forge1201ClientConfig.SHOW_PLAYER_MODEL.get(); }
    @Override public boolean showEquipmentPanel() { return Forge1201ClientConfig.SHOW_EQUIPMENT_PANEL.get(); }
    @Override public boolean showGridColumn() { return Forge1201ClientConfig.SHOW_GRID_COLUMN.get(); }
    @Override public String layoutMode() { return Forge1201ClientConfig.LAYOUT_MODE.get(); }
    @Override public int columnGap() { return Forge1201ClientConfig.COLUMN_GAP.get(); }
    @Override public boolean enableGridColumnScroll() { return Forge1201ClientConfig.ENABLE_GRID_COLUMN_SCROLL.get(); }
    @Override public boolean enableNearbyColumnScroll() { return Forge1201ClientConfig.ENABLE_NEARBY_COLUMN_SCROLL.get(); }
    @Override public int gridCellSize() { return Forge1201ClientConfig.GRID_CELL_SIZE.get(); }
    @Override public int gridItemInnerPadding() { return Forge1201ClientConfig.GRID_ITEM_INNER_PADDING.get(); }
    @Override public int equipmentFreeSlotSize() { return Forge1201ClientConfig.EQUIPMENT_FREE_SLOT_SIZE.get(); }
    @Override public int hotbarFreeSlotSize() { return Forge1201ClientConfig.HOTBAR_FREE_SLOT_SIZE.get(); }
    @Override public int freeSlotItemPadding() { return Forge1201ClientConfig.FREE_SLOT_ITEM_PADDING.get(); }
    @Override public int nearbyPanelColumns() { return Forge1201ClientConfig.NEARBY_PANEL_COLUMNS.get(); }
    @Override public int nearbyPanelVisibleRows() { return Forge1201ClientConfig.NEARBY_PANEL_VISIBLE_ROWS.get(); }
}
