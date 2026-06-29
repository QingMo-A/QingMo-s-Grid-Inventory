package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import com.dreamingfish.gridinventory.target.neoforge1211.menu.NeoForge1211MenuOpenDataCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NeoForgeGridInventoryRegistryBridge implements GridInventoryRegistryBridge {
    private final DeferredRegister.Items items = DeferredRegister.createItems(DFGridInventoryMod.MODID);
    private final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(DFGridInventoryMod.MODID);
    private final DeferredRegister<MenuType<?>> menus = DeferredRegister.create(Registries.MENU, DFGridInventoryMod.MODID);
    private final DeferredRegister<DataComponentType<?>> dataComponents = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DFGridInventoryMod.MODID);
    private final DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DFGridInventoryMod.MODID);

    public void registerItems(IEventBus modEventBus) {
        items.register(modEventBus);
    }

    public void registerBlocks(IEventBus modEventBus) {
        blocks.register(modEventBus);
    }

    public void registerMenus(IEventBus modEventBus) {
        menus.register(modEventBus);
    }

    public void registerDataComponents(IEventBus modEventBus) {
        dataComponents.register(modEventBus);
    }

    public void registerCreativeTabs(IEventBus modEventBus) {
        creativeTabs.register(modEventBus);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        return items.register(name, item);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        return blocks.register(name, block);
    }

    @Override
    public <T extends BlockItem> Supplier<T> registerBlockItem(String name, Supplier<T> item) {
        return items.register(name, item);
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory) {
        return menus.register(name, () -> new MenuType<>(
                (IContainerFactory<T>) (containerId, inventory, buffer) -> factory.create(containerId, inventory, NeoForge1211MenuOpenDataCodec.decode(buffer)),
                net.minecraft.world.flag.FeatureFlags.VANILLA_SET
        ));
    }

    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, Supplier<DataComponentType<T>> component) {
        return dataComponents.register(name, component);
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        return creativeTabs.register(name, tab);
    }
}
