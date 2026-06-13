package com.dreamingfish.gridinventory.target.forge1201.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Forge1201ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<String> PICKUP_KEY_DEFAULT;
    public static final ForgeConfigSpec.BooleanValue HIGHLIGHT_TARGET_ITEM;
    public static final ForgeConfigSpec.ConfigValue<String> HIGHLIGHT_COLOR;
    public static final ForgeConfigSpec.BooleanValue SHOW_NEARBY_ITEMS_PANEL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SURVIVAL_INVENTORY_GRID_UI;
    public static final ForgeConfigSpec.BooleanValue SHOW_PLAYER_MODEL;
    public static final ForgeConfigSpec.BooleanValue SHOW_EQUIPMENT_PANEL;
    public static final ForgeConfigSpec.BooleanValue SHOW_GRID_COLUMN;
    public static final ForgeConfigSpec.ConfigValue<String> LAYOUT_MODE;
    public static final ForgeConfigSpec.IntValue COLUMN_GAP;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GRID_COLUMN_SCROLL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_NEARBY_COLUMN_SCROLL;
    public static final ForgeConfigSpec.IntValue GRID_CELL_SIZE;
    public static final ForgeConfigSpec.IntValue GRID_ITEM_INNER_PADDING;
    public static final ForgeConfigSpec.IntValue EQUIPMENT_FREE_SLOT_SIZE;
    public static final ForgeConfigSpec.IntValue HOTBAR_FREE_SLOT_SIZE;
    public static final ForgeConfigSpec.IntValue FREE_SLOT_ITEM_PADDING;
    public static final ForgeConfigSpec.IntValue NEARBY_PANEL_COLUMNS;
    public static final ForgeConfigSpec.IntValue NEARBY_PANEL_VISIBLE_ROWS;
    public static final ForgeConfigSpec SPEC;

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
        GRID_CELL_SIZE = BUILDER.defineInRange("grid_cell_size", 27, 12, 64);
        GRID_ITEM_INNER_PADDING = BUILDER.defineInRange("grid_item_inner_padding", 2, 0, 8);
        EQUIPMENT_FREE_SLOT_SIZE = BUILDER.defineInRange("equipment_free_slot_size", 32, 18, 48);
        HOTBAR_FREE_SLOT_SIZE = BUILDER.defineInRange("hotbar_free_slot_size", 21, 18, 32);
        FREE_SLOT_ITEM_PADDING = BUILDER.defineInRange("free_slot_item_padding", 3, 0, 10);
        COLUMN_GAP = BUILDER.defineInRange("column_gap", 8, 0, 32);
        ENABLE_GRID_COLUMN_SCROLL = BUILDER.define("enable_grid_column_scroll", true);
        ENABLE_NEARBY_COLUMN_SCROLL = BUILDER.define("enable_nearby_column_scroll", true);
        NEARBY_PANEL_COLUMNS = BUILDER.defineInRange("nearby_panel_columns", 6, 1, 16);
        NEARBY_PANEL_VISIBLE_ROWS = BUILDER.defineInRange("nearby_panel_visible_rows", 10, 1, 24);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private Forge1201ClientConfig() {
    }
}
