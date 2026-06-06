package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.target.neoforge1211.registry.NeoForge1211DataComponents;
import com.dreamingfish.gridinventory.platform.GridInventoryItemStackDataBridge;
import net.minecraft.world.item.ItemStack;

public final class NeoForgeGridInventoryItemStackDataBridge implements GridInventoryItemStackDataBridge {
    @Override
    public GridInventoryData getGridInventory(ItemStack stack) {
        return stack.get(NeoForge1211DataComponents.GRID_INVENTORY.get());
    }

    @Override
    public void setGridInventory(ItemStack stack, GridInventoryData data) {
        stack.set(NeoForge1211DataComponents.GRID_INVENTORY.get(), data);
    }

    @Override
    public EquipmentStorageData getEquipmentStorage(ItemStack stack) {
        return stack.get(NeoForge1211DataComponents.EQUIPMENT_STORAGE.get());
    }

    @Override
    public void setEquipmentStorage(ItemStack stack, EquipmentStorageData data) {
        stack.set(NeoForge1211DataComponents.EQUIPMENT_STORAGE.get(), data);
    }

    @Override
    public Boolean isBackpackFolded(ItemStack stack) {
        return stack.get(NeoForge1211DataComponents.BACKPACK_FOLDED.get());
    }

    @Override
    public void setBackpackFolded(ItemStack stack, boolean folded) {
        stack.set(NeoForge1211DataComponents.BACKPACK_FOLDED.get(), folded);
    }

    @Override
    public void removeBackpackFolded(ItemStack stack) {
        stack.remove(NeoForge1211DataComponents.BACKPACK_FOLDED.get());
    }
}
