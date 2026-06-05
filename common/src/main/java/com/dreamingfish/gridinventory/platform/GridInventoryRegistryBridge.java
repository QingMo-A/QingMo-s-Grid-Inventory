package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public interface GridInventoryRegistryBridge {
    <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item);

    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory);

    Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab);

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory playerInventory, GridInventoryMenuOpenData data);
    }
}
