package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.network.chat.Component;
import com.dreamingfish.gridinventory.common.block.BasicSearchableGridContainerBlock;
import com.dreamingfish.gridinventory.common.block.SearchableGridContainerSpec;
import com.dreamingfish.gridinventory.DFGridInventory;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class ModBlocks {
    public static Supplier<Block> SEARCHABLE_GRID_CONTAINER;
    public static Supplier<BlockItem> SEARCHABLE_GRID_CONTAINER_ITEM;
    public static Supplier<Block> RAID_CONTAINER_PLACEHOLDER;
    public static Supplier<BlockItem> RAID_CONTAINER_PLACEHOLDER_ITEM;

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
                () -> new BasicSearchableGridContainerBlock(
                        Block.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD),
                        new SearchableGridContainerSpec(8, 5,
                                Component.translatable("container.df_grid_inventory.searchable_grid_container"),
                                "generic_crate", 1.0D,
                                ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID,
                                        "grid_containers/searchable_grid_container"))));
        SEARCHABLE_GRID_CONTAINER_ITEM = registry.registerBlockItem(
                GridContentIds.SEARCHABLE_GRID_CONTAINER,
                () -> new BlockItem(SEARCHABLE_GRID_CONTAINER.get(), new Item.Properties()));
        RAID_CONTAINER_PLACEHOLDER = registry.registerBlock(
                GridContentIds.RAID_CONTAINER_PLACEHOLDER,
                () -> new Block(Block.Properties.of().mapColor(MapColor.COLOR_BLUE)
                        .strength(1.0F).sound(SoundType.METAL)));
        RAID_CONTAINER_PLACEHOLDER_ITEM = registry.registerBlockItem(
                GridContentIds.RAID_CONTAINER_PLACEHOLDER,
                () -> new BlockItem(RAID_CONTAINER_PLACEHOLDER.get(), new Item.Properties()));
    }
}
