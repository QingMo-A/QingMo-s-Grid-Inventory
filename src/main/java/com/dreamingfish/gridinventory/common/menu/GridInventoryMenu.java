package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.item.SmallGridBagItem;
import com.dreamingfish.gridinventory.common.registry.ModAttachments;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class GridInventoryMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final int bagSlot;
    private final InteractionHand hand;
    private final boolean playerGrid;
    private GridInventoryData gridData;

    public GridInventoryMenu(int containerId, Inventory playerInventory, int bagSlot, InteractionHand hand, GridInventoryData gridData) {
        this(containerId, playerInventory, bagSlot, hand, gridData, false);
    }

    public GridInventoryMenu(int containerId, Inventory playerInventory, int bagSlot, InteractionHand hand, GridInventoryData gridData, boolean playerGrid) {
        super(ModMenus.GRID_INVENTORY.get(), containerId);
        this.playerInventory = playerInventory;
        this.bagSlot = bagSlot;
        this.hand = hand;
        this.playerGrid = playerGrid;
        this.gridData = gridData;
        this.gridData.setChangeListener(this::save);
        addPlayerSlots(playerInventory);
    }

    public static GridInventoryMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        int slot = buf.readVarInt();
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        boolean playerGrid = buf.readBoolean();
        GridInventoryData data = GridInventoryData.decode(buf);
        return new GridInventoryMenu(containerId, playerInventory, slot, hand, data, playerGrid);
    }

    private void addPlayerSlots(Inventory inventory) {
        if (playerGrid) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column, 8 + column * 18, 198));
            }
            return;
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        }
    }

    public GridInventoryData getGridData() {
        return gridData;
    }

    public void replaceGridData(GridInventoryData data) {
        this.gridData = data;
        this.gridData.setChangeListener(this::save);
    }

    public boolean insertFromPlayerInventory(int playerSlot, int targetX, int targetY, boolean rotated) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        if (source.isEmpty()) {
            return false;
        }
        var targetEntry = gridData.getEntries().stream().filter(entry -> entry.contains(targetX, targetY)).findFirst();
        if (targetEntry.isPresent()
                && ItemStack.isSameItemSameComponents(targetEntry.get().stack(), source)
                && targetEntry.get().stack().getCount() < targetEntry.get().stack().getMaxStackSize()) {
            int moved = Math.min(source.getCount(), targetEntry.get().stack().getMaxStackSize() - targetEntry.get().stack().getCount());
            targetEntry.get().stack().grow(moved);
            source.shrink(moved);
            gridData.setChanged();
            playerInventory.setChanged();
            save();
            return true;
        }
        if (!GridPlacementValidator.canPlace(gridData, source, targetX, targetY, rotated, null)) {
            return false;
        }
        gridData.add(source.copy(), targetX, targetY, rotated);
        source.setCount(0);
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean quickInsertFromPlayerInventory(int playerSlot) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        ItemStack remainder = gridData.insert(source.copy(), GridInsertMode.EXECUTE);
        if (remainder.isEmpty()) {
            source.setCount(0);
            playerInventory.setChanged();
            save();
            return true;
        }
        return false;
    }

    public boolean moveEntry(UUID entryId, int targetX, int targetY, boolean rotated) {
        boolean moved = gridData.move(entryId, targetX, targetY, rotated);
        if (moved) {
            save();
        }
        return moved;
    }

    public boolean extractToPlayerInventory(UUID entryId, int amount) {
        var entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack stack = entry.get().stack().copyWithCount(Math.min(amount, entry.get().stack().getCount()));
        ItemStack probe = stack.copy();
        if (!playerInventory.add(probe)) {
            return false;
        }
        gridData.extract(entryId, stack.getCount());
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean extractToPlayerSlot(UUID entryId, int playerSlot, int amount) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        var entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack target = playerInventory.getItem(playerSlot);
        ItemStack stack = entry.get().stack();
        int moveCount = Math.min(amount, stack.getCount());
        if (target.isEmpty()) {
            ItemStack extracted = gridData.extract(entryId, moveCount);
            playerInventory.setItem(playerSlot, extracted);
            playerInventory.setChanged();
            save();
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(target, stack) || target.getCount() >= target.getMaxStackSize()) {
            return false;
        }
        int accepted = Math.min(moveCount, target.getMaxStackSize() - target.getCount());
        if (accepted <= 0) {
            return false;
        }
        gridData.extract(entryId, accepted);
        target.grow(accepted);
        playerInventory.setChanged();
        save();
        return true;
    }

    public ItemStack bagStack() {
        if (playerGrid) {
            return ItemStack.EMPTY;
        }
        return hand == InteractionHand.MAIN_HAND ? playerInventory.getItem(bagSlot) : playerInventory.player.getOffhandItem();
    }

    public void save() {
        if (playerGrid) {
            playerInventory.player.setData(ModAttachments.PLAYER_GRID_INVENTORY, gridData.copy());
            return;
        }
        ItemStack stack = bagStack();
        if (!stack.isEmpty()) {
            SmallGridBagItem.setData(stack, gridData);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= 0 && index < slots.size()) {
            int playerSlot = slots.get(index).getSlotIndex();
            quickInsertFromPlayerInventory(playerSlot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return playerGrid || !bagStack().isEmpty();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        save();
    }
}
