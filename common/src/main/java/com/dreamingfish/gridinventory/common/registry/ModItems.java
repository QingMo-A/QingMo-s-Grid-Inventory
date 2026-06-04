package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.item.SmallGridBagItem;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class ModItems {
    public static final Supplier<Item> SMALL_GRID_BAG = GridInventoryServices.registry().registerItem("small_grid_bag", () -> new SmallGridBagItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> GRID_BACKPACK = GridInventoryServices.registry().registerItem("grid_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> GRAY_FIELD_BACKPACK = GridInventoryServices.registry().registerItem("gray_field_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> LEATHER_BACKPACK = GridInventoryServices.registry().registerItem("leather_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> LIME_HIKING_BACKPACK = GridInventoryServices.registry().registerItem("lime_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MEDIUM_HIKING_BACKPACK = GridInventoryServices.registry().registerItem("medium_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MILITARY_HIKING_BACKPACK = GridInventoryServices.registry().registerItem("military_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> TACTICAL_BACKPACK = GridInventoryServices.registry().registerItem("tactical_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }

    public static void bootstrap() {
    }
}
