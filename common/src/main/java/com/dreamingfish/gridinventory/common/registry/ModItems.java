package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.item.SmallGridBagItem;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class ModItems {
    public static Supplier<Item> SMALL_GRID_BAG;
    public static Supplier<Item> GRID_BACKPACK;
    public static Supplier<Item> GRAY_FIELD_BACKPACK;
    public static Supplier<Item> LEATHER_BACKPACK;
    public static Supplier<Item> LIME_HIKING_BACKPACK;
    public static Supplier<Item> MEDIUM_HIKING_BACKPACK;
    public static Supplier<Item> MILITARY_HIKING_BACKPACK;
    public static Supplier<Item> TACTICAL_BACKPACK;

    private static boolean registered;

    private ModItems() {
    }

    public static void register(GridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;

        SMALL_GRID_BAG = registry.registerItem("small_grid_bag", () -> new SmallGridBagItem(new Item.Properties().stacksTo(1)));
        GRID_BACKPACK = registry.registerItem("grid_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
        GRAY_FIELD_BACKPACK = registry.registerItem("gray_field_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
        LEATHER_BACKPACK = registry.registerItem("leather_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
        LIME_HIKING_BACKPACK = registry.registerItem("lime_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
        MEDIUM_HIKING_BACKPACK = registry.registerItem("medium_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
        MILITARY_HIKING_BACKPACK = registry.registerItem("military_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
        TACTICAL_BACKPACK = registry.registerItem("tactical_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    }
}
