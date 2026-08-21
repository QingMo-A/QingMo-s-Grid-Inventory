package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridExplicitInsertHelper;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.GridStackMerger;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.pickup.ManualPickupHandler;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.UUID;

public class SearchableGridContainerMenu extends GridInventoryMenu {
    private static final double MAX_DISTANCE_SQUARED = 64.0D;

    private final Inventory playerInventory;
    private final BlockPos blockPos;
    private final Component containerTitle;
    private final boolean playerCreative;
    private GridInventoryData containerGridData;

    public SearchableGridContainerMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
                                       GridInventoryData playerGrid, GridInventoryData containerGrid, Component containerTitle,
                                       boolean playerCreative) {
        super(ModMenus.SEARCHABLE_GRID_CONTAINER.get(), containerId, playerInventory, -1,
                InteractionHand.MAIN_HAND, playerGrid.copy(), true);
        this.playerInventory = playerInventory;
        this.blockPos = blockPos;
        this.containerTitle = containerTitle;
        this.playerCreative = playerCreative;
        this.containerGridData = containerGrid.copy();
        this.containerGridData.setChangeListener(this::saveContainerGrid);
    }

    public static SearchableGridContainerMenu fromOpenData(int containerId, Inventory playerInventory,
                                                           SearchableGridContainerMenuOpenData data) {
        Component title = Component.translatable(data.titleKey());
        return new SearchableGridContainerMenu(containerId, playerInventory, data.blockPos(),
                data.playerGrid(), data.containerGrid(), title, data.creativeMode());
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public Component containerTitle() {
        return containerTitle;
    }

    public boolean playerCreative() {
        return playerCreative;
    }

    public GridInventoryData getContainerGridData() {
        return containerGridData;
    }

    public void replaceContainerGridData(GridInventoryData data) {
        containerGridData = data.copy();
        containerGridData.setChangeListener(this::saveContainerGrid);
    }

    @Override
    public void save() {
        GridInventoryServices.playerData().setPlayerGridInventory(playerInventory.player, getGridData().copy());
    }

    public void saveContainerGrid() {
        Level level = playerInventory.player.level();
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SearchableGridContainerBlockEntity container) {
            container.setGridData(containerGridData.copy());
            container.setChanged();
        }
    }

    public boolean insertPlayerSlotIntoContainerGrid(int playerSlot, int targetX, int targetY, boolean rotated, boolean folded) {
        if (!validContainerAccess() || playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        if (source.isEmpty()) {
            return false;
        }
        ItemStack incoming = source.copy();
        if (!applyBackpackFolded(incoming, folded)) {
            return false;
        }
        int moved = GridExplicitInsertHelper.insertOrMergeAt(containerGridData, incoming, targetX, targetY, rotated, 0);
        if (moved <= 0) {
            return false;
        }
        source.shrink(moved);
        playerInventory.setChanged();
        saveContainerGrid();
        save();
        return true;
    }

    public boolean moveContainerEntry(UUID entryId, int targetX, int targetY, boolean rotated, boolean folded) {
        if (!validContainerAccess()) {
            return false;
        }
        Optional<GridEntry> entry = containerGridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack source = entry.get().stack();
        ItemStack moving = source.copy();
        if (!applyBackpackFolded(moving, folded)) {
            return false;
        }
        Optional<GridEntry> targetEntry = containerGridData.getEntries().stream()
                .filter(candidate -> !candidate.entryId().equals(entryId))
                .filter(candidate -> candidate.contains(targetX, targetY))
                .findFirst();
        if (targetEntry.isPresent()) {
            ItemStack target = targetEntry.get().stack();
            if (!GridStackMerger.itemsStackableInGrid() || !GridItemStacks.sameItemSameData(target, moving)
                    || target.getCount() >= target.getMaxStackSize()) {
                return false;
            }
            int moved = Math.min(moving.getCount(), target.getMaxStackSize() - target.getCount());
            if (moved <= 0) {
                return false;
            }
            applyBackpackFolded(source, folded);
            target.grow(moved);
            containerGridData.extract(entryId, moved);
            saveContainerGrid();
            return true;
        }
        if (!GridPlacementValidator.canPlace(containerGridData, moving, targetX, targetY, rotated, entryId, 0)) {
            return false;
        }
        boolean previousFolded = GridBackpackItem.isFolded(source);
        applyBackpackFolded(source, folded);
        boolean moved = containerGridData.move(entryId, targetX, targetY, rotated);
        if (moved) {
            saveContainerGrid();
        } else {
            applyBackpackFolded(source, previousFolded);
        }
        return moved;
    }

    public boolean extractContainerEntryToPlayerSlot(UUID entryId, int playerSlot, int amount) {
        if (!validContainerAccess() || playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        Optional<GridEntry> entry = containerGridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack source = entry.get().stack();
        Slot slotView = slots.stream().filter(slot -> slot.getSlotIndex() == playerSlot).findFirst().orElse(null);
        if (slotView != null && !slotView.mayPlace(source)) {
            return false;
        }
        ItemStack target = playerInventory.getItem(playerSlot);
        int moveCount = Math.min(Math.max(1, amount), source.getCount());
        if (target.isEmpty()) {
            int limit = slotView == null ? source.getMaxStackSize() : Math.min(slotView.getMaxStackSize(), source.getMaxStackSize());
            ItemStack extracted = containerGridData.extract(entryId, Math.min(moveCount, limit));
            playerInventory.setItem(playerSlot, extracted);
            playerInventory.setChanged();
            saveContainerGrid();
            return true;
        }
        if (!GridItemStacks.sameItemSameData(target, source) || target.getCount() >= target.getMaxStackSize()) {
            return false;
        }
        int accepted = Math.min(moveCount, target.getMaxStackSize() - target.getCount());
        if (accepted <= 0) {
            return false;
        }
        containerGridData.extract(entryId, accepted);
        target.grow(accepted);
        playerInventory.setChanged();
        saveContainerGrid();
        return true;
    }

    public boolean pickupGroundItemIntoContainerGrid(int entityId, int targetX, int targetY,
                                                     boolean rotated, boolean folded) {
        if (!validContainerAccess() || !(playerInventory.player instanceof ServerPlayer player)) {
            return false;
        }
        Optional<ItemEntity> itemEntity = ManualPickupHandler.findReachableItem(player, entityId);
        if (itemEntity.isEmpty()) {
            return false;
        }
        ItemStack source = itemEntity.get().getItem();
        ItemStack incoming = source.copy();
        if (source.isEmpty() || !applyBackpackFolded(incoming, folded)) {
            return false;
        }
        int moved = GridExplicitInsertHelper.insertOrMergeAt(
                containerGridData, incoming, targetX, targetY, rotated, 0);
        if (moved <= 0) {
            return false;
        }
        source.shrink(moved);
        player.take(itemEntity.get(), moved);
        if (source.isEmpty()) {
            itemEntity.get().discard();
        }
        saveContainerGrid();
        return true;
    }

    public boolean creativeInsertIntoContainerGrid(int tabIndex, int itemIndex, int count,
                                                   int targetX, int targetY, boolean rotated, boolean folded) {
        if (!validContainerAccess()) {
            return false;
        }
        Optional<ItemStack> stack = creativeStack(tabIndex, itemIndex, creativeGridCount(count), folded);
        if (stack.isEmpty()) {
            return false;
        }
        int moved = GridExplicitInsertHelper.insertOrMergeAt(
                containerGridData, stack.get(), targetX, targetY, rotated, 0);
        if (moved <= 0) {
            return false;
        }
        saveContainerGrid();
        return true;
    }

    private boolean validContainerAccess() {
        Player player = playerInventory.player;
        if (player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D)
                > MAX_DISTANCE_SQUARED) {
            return false;
        }
        if (!(player.level().getBlockEntity(blockPos) instanceof SearchableGridContainerBlockEntity container)) {
            return false;
        }
        containerGridData = container.getGridData().copy();
        containerGridData.setChangeListener(this::saveContainerGrid);
        return true;
    }

    private static boolean applyBackpackFolded(ItemStack stack, boolean folded) {
        if (!(stack.getItem() instanceof GridBackpackItem)) {
            return true;
        }
        if (folded && !GridBackpackItem.canFold(stack)) {
            return false;
        }
        GridInventoryServices.itemStackData().setBackpackFolded(stack, folded);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) > MAX_DISTANCE_SQUARED) {
            return false;
        }
        return player.level().getBlockEntity(blockPos) instanceof SearchableGridContainerBlockEntity;
    }
}
