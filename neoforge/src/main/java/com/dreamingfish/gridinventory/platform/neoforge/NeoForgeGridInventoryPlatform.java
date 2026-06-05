package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryPlatform;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class NeoForgeGridInventoryPlatform implements GridInventoryPlatform {
    private final GridInventoryNetworkBridge network = new NeoForgeGridInventoryNetworkBridge();
    private final GridInventoryPlayerDataBridge playerData = new NeoForgeGridInventoryPlayerDataBridge();
    private final GridInventoryAccessoryBridge accessories = new NeoForgeCuriosAccessoryBridge();
    private final NeoForgeGridInventoryRegistryBridge registry = new NeoForgeGridInventoryRegistryBridge();
    private final GridInventoryMenuBridge menus = new NeoForgeGridInventoryMenuBridge();
    private final GridInventoryConfigAccess config = new NeoForgeGridInventoryConfigAccess();
    private final GridInventoryClientConfigAccess clientConfig = new NeoForgeGridInventoryClientConfigAccess();

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, Player player) {
        return stack.canEquip(slot, player);
    }

    @Override
    public GridInventoryNetworkBridge network() {
        return network;
    }

    @Override
    public GridInventoryPlayerDataBridge playerData() {
        return playerData;
    }

    @Override
    public GridInventoryAccessoryBridge accessories() {
        return accessories;
    }

    @Override
    public NeoForgeGridInventoryRegistryBridge registry() {
        return registry;
    }

    @Override
    public GridInventoryMenuBridge menus() {
        return menus;
    }

    @Override
    public GridInventoryConfigAccess config() {
        return config;
    }

    @Override
    public GridInventoryClientConfigAccess clientConfig() {
        return clientConfig;
    }
}
