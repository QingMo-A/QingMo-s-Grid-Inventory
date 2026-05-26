package com.dreamingfish.gridinventory.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GridInventoryClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> PICKUP_KEY_DEFAULT;
    public static final ModConfigSpec.BooleanValue HIGHLIGHT_TARGET_ITEM;
    public static final ModConfigSpec.ConfigValue<String> HIGHLIGHT_COLOR;
    public static final ModConfigSpec.BooleanValue SHOW_NEARBY_ITEMS_PANEL;
    public static final ModConfigSpec.BooleanValue ENABLE_SURVIVAL_INVENTORY_GRID_UI;
    public static final ModConfigSpec.BooleanValue SHOW_PLAYER_MODEL;
    public static final ModConfigSpec.BooleanValue SHOW_EQUIPMENT_PANEL;
    public static final ModConfigSpec.BooleanValue SHOW_GRID_COLUMN;
    public static final ModConfigSpec.ConfigValue<String> LAYOUT_MODE;
    public static final ModConfigSpec.IntValue COLUMN_GAP;
    public static final ModConfigSpec.BooleanValue ENABLE_GRID_COLUMN_SCROLL;
    public static final ModConfigSpec.BooleanValue ENABLE_NEARBY_COLUMN_SCROLL;
    public static final ModConfigSpec.IntValue GRID_CELL_SIZE;
    public static final ModConfigSpec.IntValue GRID_ITEM_INNER_PADDING;
    public static final ModConfigSpec.IntValue NEARBY_PANEL_COLUMNS;
    public static final ModConfigSpec.IntValue NEARBY_PANEL_VISIBLE_ROWS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("client_pickup");
        PICKUP_KEY_DEFAULT = BUILDER.define("pickup_key_default", "R");
        HIGHLIGHT_TARGET_ITEM = BUILDER.define("highlight_target_item", true);
        HIGHLIGHT_COLOR = BUILDER.define("highlight_color", "FFFFFF");
        SHOW_NEARBY_ITEMS_PANEL = BUILDER.define("show_nearby_items_panel", true);
        BUILDER.pop();

        BUILDER.push("client_ui");
        ENABLE_SURVIVAL_INVENTORY_GRID_UI = BUILDER.define("enable_survival_inventory_grid_ui", true);
        LAYOUT_MODE = BUILDER.define("layout_mode", "three_columns");
        SHOW_PLAYER_MODEL = BUILDER.define("show_player_model", true);
        SHOW_EQUIPMENT_PANEL = BUILDER.define("show_equipment_panel", true);
        SHOW_GRID_COLUMN = BUILDER.define("show_grid_column", true);
        BUILDER.define("show_nearby_items_panel", true);
        GRID_CELL_SIZE = BUILDER.defineInRange("grid_cell_size", 18, 12, 32);
        GRID_ITEM_INNER_PADDING = BUILDER.comment("Inner spacing, in pixels, between an enlarged grid item icon and its occupied area.")
                .defineInRange("grid_item_inner_padding", 2, 0, 8);
        COLUMN_GAP = BUILDER.defineInRange("column_gap", 8, 0, 32);
        ENABLE_GRID_COLUMN_SCROLL = BUILDER.define("enable_grid_column_scroll", true);
        ENABLE_NEARBY_COLUMN_SCROLL = BUILDER.define("enable_nearby_column_scroll", true);
        NEARBY_PANEL_COLUMNS = BUILDER.defineInRange("nearby_panel_columns", 6, 1, 16);
        NEARBY_PANEL_VISIBLE_ROWS = BUILDER.defineInRange("nearby_panel_visible_rows", 10, 1, 24);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private GridInventoryClientConfig() {
    }
}
