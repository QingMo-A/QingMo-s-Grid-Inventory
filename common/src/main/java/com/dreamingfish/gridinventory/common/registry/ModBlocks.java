package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public final class ModBlocks {
    public static Supplier<Block> SEARCHABLE_GRID_CONTAINER;
    public static Supplier<BlockItem> SEARCHABLE_GRID_CONTAINER_ITEM;

    private static boolean registered;

    private ModBlocks() {
    }

    public static void register(GridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;

        SEARCHABLE_GRID_CONTAINER = registry.registerBlock(
                GridContentIds.SEARCHABLE_GRID_CONTAINER,
                () -> new Block(Block.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(2.5F)
                        .sound(SoundType.WOOD)));
        SEARCHABLE_GRID_CONTAINER_ITEM = registry.registerBlockItem(
                GridContentIds.SEARCHABLE_GRID_CONTAINER,
                () -> new BlockItem(SEARCHABLE_GRID_CONTAINER.get(), new Item.Properties()));
    }
}
