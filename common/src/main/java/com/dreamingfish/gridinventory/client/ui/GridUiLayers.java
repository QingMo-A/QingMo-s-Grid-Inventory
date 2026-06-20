package com.dreamingfish.gridinventory.client.ui;

public final class GridUiLayers {
    public static final float MAIN = 0.0F;
    public static final float MAIN_HOVER = 8.0F;
    public static final float EQUIPMENT_TOOLTIP = 200.0F;
    public static final float EQUIPMENT_TOOLTIP_ITEM = 210.0F;
    public static final float NESTED_WINDOW_BASE = 400.0F;
    // Leaves room for vanilla item renderer and item decoration depth offsets inside each window.
    public static final float NESTED_WINDOW_STEP = 250.0F;
    public static final float NESTED_WINDOW_BACKGROUND = 0.0F;
    public static final float NESTED_WINDOW_SHADOW = 1.0F;
    public static final float NESTED_WINDOW_HEADER = 10.0F;
    public static final float NESTED_WINDOW_HEADER_ICON = 20.0F;
    public static final float NESTED_WINDOW_SECTION_LABEL = 30.0F;
    public static final float NESTED_WINDOW_GRID = 40.0F;
    public static final float NESTED_WINDOW_ITEM = 80.0F;
    public static final float NESTED_WINDOW_ITEM_DECORATION = 100.0F;
    public static final float NESTED_WINDOW_OVERLAY = 130.0F;
    public static final float DRAGGED_ITEM_ABOVE_NESTED_WINDOW = 80.0F;
    public static final float MAX_SAFE_GUI_ITEM_Z = 2500.0F;
    public static final float DRAGGED_ITEM = 2400.0F;
    public static final float VANILLA_TOOLTIP = 2600.0F;

    public static float draggedItemZ(float topNestedWindowZ) {
        return Math.min(topNestedWindowZ + DRAGGED_ITEM_ABOVE_NESTED_WINDOW, MAX_SAFE_GUI_ITEM_Z);
    }

    private GridUiLayers() {
    }
}
