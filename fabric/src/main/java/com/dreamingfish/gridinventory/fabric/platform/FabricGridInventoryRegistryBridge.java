package com.dreamingfish.gridinventory.fabric.platform;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class FabricGridInventoryRegistryBridge implements GridInventoryRegistryBridge {
    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        T registered = Registry.register(BuiltInRegistries.ITEM, id(name), item.get());
        return () -> registered;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory) {
        MenuType<T> menuType = new ExtendedScreenHandlerType<>((containerId, inventory, data) -> factory.create(containerId, inventory, data), GridInventoryMenuOpenData.STREAM_CODEC);
        MenuType<T> registered = Registry.register(BuiltInRegistries.MENU, id(name), menuType);
        return () -> registered;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, Supplier<DataComponentType<T>> component) {
        DataComponentType<T> registered = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id(name), component.get());
        return () -> registered;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        CreativeModeTab registered = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id(name), tab.get());
        return () -> registered;
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, name);
    }
}
