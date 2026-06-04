package com.dreamingfish.gridinventory.api;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public interface IGridInventory {
    int getColumns();
    int getRows();
    List<GridEntry> getEntries();
    boolean canInsert(ItemStack stack);
    ItemStack insert(ItemStack stack, GridInsertMode mode);
    ItemStack extract(UUID entryId, int amount);
    void setChanged();
}
