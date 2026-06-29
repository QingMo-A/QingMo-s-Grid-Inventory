package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public interface GridInventoryRegistryBridge {
    <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item);

    <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block);

    <T extends BlockItem> Supplier<T> registerBlockItem(String name, Supplier<T> item);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name,
            BlockEntitySupplier<T> factory,
            Supplier<? extends Block> validBlock);

    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory);

    Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab);

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory playerInventory, GridInventoryMenuOpenData data);
    }

    @FunctionalInterface
    interface BlockEntitySupplier<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}
