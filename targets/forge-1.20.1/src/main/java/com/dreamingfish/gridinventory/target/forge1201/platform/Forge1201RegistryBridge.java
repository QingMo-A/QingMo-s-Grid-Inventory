package com.dreamingfish.gridinventory.target.forge1201.platform;

import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class Forge1201RegistryBridge implements GridInventoryRegistryBridge {
    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return unsupported("item", name);
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory) {
        return unsupported("menu", name);
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        return unsupported("creative tab", name);
    }

    private static <T> Supplier<T> unsupported(String type, String name) {
        return () -> {
            throw new UnsupportedOperationException("Forge 1.20.1 registry bridge has not implemented " + type + " registration for " + name);
        };
    }
}
