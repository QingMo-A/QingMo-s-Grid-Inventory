package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.item.SmallGridBagItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DFGridInventoryMod.MODID);
    public static final DeferredItem<Item> SMALL_GRID_BAG = ITEMS.register("small_grid_bag", () -> new SmallGridBagItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GRID_BACKPACK = ITEMS.register("grid_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GRAY_FIELD_BACKPACK = ITEMS.register("gray_field_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LEATHER_BACKPACK = ITEMS.register("leather_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LIME_HIKING_BACKPACK = ITEMS.register("lime_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MEDIUM_HIKING_BACKPACK = ITEMS.register("medium_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MILITARY_HIKING_BACKPACK = ITEMS.register("military_hiking_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> TACTICAL_BACKPACK = ITEMS.register("tactical_backpack", () -> new GridBackpackItem(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
