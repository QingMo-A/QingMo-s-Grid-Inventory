package com.dreamingfish.gridinventory.target.neoforge1211.compat.inventory;

import com.dreamingfish.gridinventory.common.inventory.compat.GridItemHandlerMutationCommitter;
import com.dreamingfish.gridinventory.common.inventory.compat.PlayerGridInventoryAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public final class NeoForge1211PlayerInventoryCompatHandler implements IItemHandlerModifiable, GridItemHandlerMutationCommitter {
    private final IItemHandler vanilla;
    private final PlayerGridInventoryAccess grid;

    public NeoForge1211PlayerInventoryCompatHandler(Player player, IItemHandler vanilla) {
        this.vanilla = vanilla;
        this.grid = new PlayerGridInventoryAccess(player);
    }

    @Override
    public int getSlots() {
        return vanilla.getSlots() + grid.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isVanillaSlot(slot) ? vanilla.getStackInSlot(slot) : grid.borrowStackInSlot(gridSlot(slot));
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (isVanillaSlot(slot)) {
            if (vanilla instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack);
            }
        } else {
            grid.setStackInSlot(gridSlot(slot), stack);
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isVanillaSlot(slot)
                ? vanilla.insertItem(slot, stack, simulate)
                : grid.insertItem(gridSlot(slot), stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return isVanillaSlot(slot)
                ? vanilla.extractItem(slot, amount, simulate)
                : grid.extractItem(gridSlot(slot), amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return isVanillaSlot(slot) ? vanilla.getSlotLimit(slot) : grid.getSlotLimit(gridSlot(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return isVanillaSlot(slot) ? vanilla.isItemValid(slot, stack) : grid.isItemValid(gridSlot(slot), stack);
    }

    @Override
    public void commitExternalMutations() {
        grid.commitExternalMutations();
    }

    private boolean isVanillaSlot(int slot) {
        return slot >= 0 && slot < vanilla.getSlots();
    }

    private int gridSlot(int slot) {
        return slot - vanilla.getSlots();
    }
}
