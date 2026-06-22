package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.client.creative.VanillaCreativeTabProvider;
import com.dreamingfish.gridinventory.client.creative.VanillaCreativeTabsSnapshot;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryItemStackDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryPlatform;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import com.dreamingfish.gridinventory.target.forge1201.config.Forge1201ClientConfigAccess;
import com.dreamingfish.gridinventory.target.forge1201.config.Forge1201ServerConfigAccess;
import com.dreamingfish.gridinventory.target.forge1201.platform.Forge1201RegistryBridge;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class Forge1201Platform implements GridInventoryPlatform {
    private final GridInventoryItemStackDataBridge itemStackData = new Forge1201ItemStackDataBridge();
    private final GridInventoryPlayerDataBridge playerData = new Forge1201PlayerDataBridge();
    private final GridInventoryAccessoryBridge accessories = new Forge1201AccessoryBridge();
    private final GridInventoryNetworkBridge network = new Forge1201NetworkBridge();
    private final Forge1201RegistryBridge registry = new Forge1201RegistryBridge();
    private final GridInventoryMenuBridge menus = new Forge1201MenuBridge();
    private final GridInventoryConfigAccess config = new Forge1201ServerConfigAccess();
    private final GridInventoryClientConfigAccess clientConfig = new Forge1201ClientConfigAccess();
    private final VanillaCreativeTabProvider creativeTabs = new VanillaCreativeTabsSnapshot();

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, Player player) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot() == slot;
        }
        return slot == EquipmentSlot.CHEST && stack.getItem() instanceof ElytraItem;
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
    public GridInventoryItemStackDataBridge itemStackData() {
        return itemStackData;
    }

    @Override
    public Forge1201RegistryBridge registry() {
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

    @Override
    public VanillaCreativeTabProvider creativeTabs() {
        return creativeTabs;
    }
}
