package com.dreamingfish.gridinventory.platform.config;

public interface GridInventoryClientConfigAccess {
    String pickupKeyDefault();

    boolean highlightTargetItem();

    String highlightColor();

    boolean showNearbyItemsPanel();

    boolean enableSurvivalInventoryGridUi();

    boolean showPlayerModel();

    boolean showEquipmentPanel();

    boolean showGridColumn();

    String layoutMode();

    int columnGap();

    boolean enableGridColumnScroll();

    boolean enableNearbyColumnScroll();

    int gridCellSize();

    int gridItemInnerPadding();

    int equipmentFreeSlotSize();

    int hotbarFreeSlotSize();

    int freeSlotItemPadding();

    int nearbyPanelColumns();

    int nearbyPanelVisibleRows();
}
