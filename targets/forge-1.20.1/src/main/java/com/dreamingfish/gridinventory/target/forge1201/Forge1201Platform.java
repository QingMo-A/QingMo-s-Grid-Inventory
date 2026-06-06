package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryItemStackDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryPlatform;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import com.dreamingfish.gridinventory.target.forge1201.platform.Forge1201RegistryBridge;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class Forge1201Platform implements GridInventoryPlatform {
    private final GridInventoryItemStackDataBridge itemStackData = new Forge1201ItemStackDataBridge();
    private final GridInventoryRegistryBridge registry = new Forge1201RegistryBridge();

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
        throw unsupported("NetworkBridge");
    }

    @Override
    public GridInventoryPlayerDataBridge playerData() {
        throw unsupported("PlayerDataBridge");
    }

    @Override
    public GridInventoryAccessoryBridge accessories() {
        throw unsupported("AccessoryBridge");
    }

    @Override
    public GridInventoryItemStackDataBridge itemStackData() {
        return itemStackData;
    }

    @Override
    public GridInventoryRegistryBridge registry() {
        return registry;
    }

    @Override
    public GridInventoryMenuBridge menus() {
        throw unsupported("MenuBridge");
    }

    @Override
    public GridInventoryConfigAccess config() {
        throw unsupported("ConfigAccess");
    }

    @Override
    public GridInventoryClientConfigAccess clientConfig() {
        throw unsupported("ClientConfigAccess");
    }

    private static UnsupportedOperationException unsupported(String bridge) {
        return new UnsupportedOperationException("Forge 1.20.1 " + bridge + " is not implemented yet");
    }
}
