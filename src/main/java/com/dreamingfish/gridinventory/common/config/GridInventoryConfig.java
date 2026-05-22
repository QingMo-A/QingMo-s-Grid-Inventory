package com.dreamingfish.gridinventory.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GridInventoryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_GRID_INVENTORY;
    public static final ModConfigSpec.IntValue DEFAULT_ITEM_WIDTH;
    public static final ModConfigSpec.IntValue DEFAULT_ITEM_HEIGHT;
    public static final ModConfigSpec.BooleanValue DEFAULT_ROTATABLE;
    public static final ModConfigSpec.IntValue SMALL_GRID_BAG_COLUMNS;
    public static final ModConfigSpec.IntValue SMALL_GRID_BAG_ROWS;
    public static final ModConfigSpec.BooleanValue REPLACE_SURVIVAL_INVENTORY;
    public static final ModConfigSpec.IntValue PLAYER_GRID_COLUMNS;
    public static final ModConfigSpec.IntValue PLAYER_GRID_ROWS;
    public static final ModConfigSpec.BooleanValue CUSTOM_HOTBAR_SLOTS_ENABLED;
    public static final ModConfigSpec.IntValue HOTBAR_SLOTS;
    public static final ModConfigSpec.BooleanValue DISABLE_VANILLA_AUTO_PICKUP;
    public static final ModConfigSpec.BooleanValue MANUAL_PICKUP_ENABLED;
    public static final ModConfigSpec.DoubleValue PICKUP_RANGE;
    public static final ModConfigSpec.DoubleValue NEARBY_ITEMS_RANGE;
    public static final ModConfigSpec.BooleanValue ALLOW_PICKUP_THROUGH_WALLS;
    public static final ModConfigSpec.BooleanValue SERVER_VALIDATE_NEARBY_RANGE;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("general");
        ENABLE_GRID_INVENTORY = BUILDER.define("enable_grid_inventory", true);
        DEFAULT_ITEM_WIDTH = BUILDER.defineInRange("default_item_width", 1, 1, 32);
        DEFAULT_ITEM_HEIGHT = BUILDER.defineInRange("default_item_height", 1, 1, 32);
        DEFAULT_ROTATABLE = BUILDER.define("default_rotatable", false);
        BUILDER.pop();

        BUILDER.push("small_grid_bag");
        SMALL_GRID_BAG_COLUMNS = BUILDER.defineInRange("columns", 8, 1, 32);
        SMALL_GRID_BAG_ROWS = BUILDER.defineInRange("rows", 6, 1, 32);
        BUILDER.pop();

        BUILDER.push("player_grid_inventory");
        REPLACE_SURVIVAL_INVENTORY = BUILDER.define("replace_survival_inventory", true);
        PLAYER_GRID_COLUMNS = BUILDER.defineInRange("columns", 10, 1, 32);
        PLAYER_GRID_ROWS = BUILDER.defineInRange("rows", 6, 1, 32);
        BUILDER.pop();

        BUILDER.push("hotbar");
        CUSTOM_HOTBAR_SLOTS_ENABLED = BUILDER.define("custom_hotbar_slots_enabled", false);
        HOTBAR_SLOTS = BUILDER.defineInRange("hotbar_slots", 9, 1, 18);
        BUILDER.pop();

        BUILDER.push("pickup");
        DISABLE_VANILLA_AUTO_PICKUP = BUILDER.define("disable_vanilla_auto_pickup", true);
        MANUAL_PICKUP_ENABLED = BUILDER.define("manual_pickup_enabled", true);
        PICKUP_RANGE = BUILDER.defineInRange("pickup_range", 4.0, 0.5, 32.0);
        NEARBY_ITEMS_RANGE = BUILDER.defineInRange("nearby_items_range", 6.0, 0.5, 64.0);
        ALLOW_PICKUP_THROUGH_WALLS = BUILDER.define("allow_pickup_through_walls", false);
        BUILDER.pop();

        BUILDER.push("nearby_panel");
        SERVER_VALIDATE_NEARBY_RANGE = BUILDER.define("server_validate_nearby_range", true);
        BUILDER.defineInRange("nearby_items_range", 6.0, 0.5, 64.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private GridInventoryConfig() {
    }
}
