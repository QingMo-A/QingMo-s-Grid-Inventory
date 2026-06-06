package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.item.ItemStack;

public interface GridInventoryItemStackDataBridge {
    GridInventoryData getGridInventory(ItemStack stack);

    void setGridInventory(ItemStack stack, GridInventoryData data);

    void removeGridInventory(ItemStack stack);

    EquipmentStorageData getEquipmentStorage(ItemStack stack);

    void setEquipmentStorage(ItemStack stack, EquipmentStorageData data);

    void removeEquipmentStorage(ItemStack stack);

    Boolean isBackpackFolded(ItemStack stack);

    void setBackpackFolded(ItemStack stack, boolean folded);

    void removeBackpackFolded(ItemStack stack);
}
