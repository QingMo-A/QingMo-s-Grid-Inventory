package com.dreamingfish.gridinventory.common.inventory.compat;

import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridExplicitInsertHelper;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.GridStackMerger;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionManager;
import com.dreamingfish.gridinventory.common.network.SyncExternalPlayerGridMessage;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A transactional view of the top-level player grid for vanilla and mod inventory bridges.
 * Nested containers deliberately remain behind the normal grid transfer protocol.
 */
public final class PlayerGridInventoryAccess {
    private final Player player;
    private final Map<Integer, BorrowedStack> borrowedStacks = new HashMap<>();
    private GridInventoryData grid;
    private int loadedInventoryRevision = Integer.MIN_VALUE;

    public PlayerGridInventoryAccess(Player player) {
        this.player = player;
    }

    public static boolean isEnabled(Player player) {
        return player != null
                && GridInventoryServices.config().enableGridInventory()
                && GridInventoryServices.config().replaceSurvivalInventory();
    }

    public synchronized int getSlots() {
        GridInventoryData current = currentGrid();
        return current.getColumns() * current.getRows();
    }

    public synchronized ItemStack borrowStackInSlot(int slot) {
        Optional<GridEntry> entry = entryAtAnchor(currentGrid(), slot);
        if (entry.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = entry.get().stack().copy();
        ItemStack exposed = original.copy();
        borrowedStacks.put(slot, new BorrowedStack(entry.get().entryId(), original, exposed));
        return exposed;
    }

    public synchronized int getSlotLimit(int slot) {
        GridInventoryData current = currentGrid();
        if (!validSlot(current, slot)
                || !current.isEnabledCell(slotX(current, slot), slotY(current, slot))
                || isCoveredNonAnchor(current, slot)) {
            return 0;
        }
        return GridStackMerger.itemsStackableInGrid() ? 64 : 1;
    }

    public synchronized ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !validSlot(currentGrid(), slot) || isCoveredNonAnchor(currentGrid(), slot)) {
            return stack;
        }
        if (!simulate) {
            commitExternalMutations();
        }
        if (isCoveredNonAnchor(currentGrid(), slot)) {
            return stack;
        }
        GridInventoryData updated = currentGrid().copy();
        int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
        if (!GridStackMerger.itemsStackableInGrid()) {
            amount = Math.min(amount, 1);
        }
        ItemStack candidate = stack.copyWithCount(amount);
        int moved = GridExplicitInsertHelper.insertOrMergeAt(
                updated, candidate, slotX(updated, slot), slotY(updated, slot), false, 0);
        if (moved <= 0) {
            return stack;
        }
        if (!simulate) {
            save(updated);
        }
        ItemStack remainder = stack.copy();
        remainder.shrink(moved);
        return remainder;
    }

    public synchronized ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            commitExternalMutations();
        }
        Optional<GridEntry> entry = entryAtAnchor(currentGrid(), slot);
        if (entry.isEmpty()) {
            return ItemStack.EMPTY;
        }
        GridInventoryData updated = currentGrid().copy();
        ItemStack extracted = updated.extract(entry.get().entryId(), amount);
        if (!simulate && !extracted.isEmpty()) {
            save(updated);
        }
        return extracted;
    }

    public synchronized void setStackInSlot(int slot, ItemStack stack) {
        if (!validSlot(currentGrid(), slot) || isCoveredNonAnchor(currentGrid(), slot)) {
            return;
        }
        commitExternalMutations();
        if (isCoveredNonAnchor(currentGrid(), slot)) {
            return;
        }
        GridInventoryData updated = currentGrid().copy();
        Optional<GridEntry> existing = entryAtAnchor(updated, slot);
        if (stack.isEmpty()) {
            existing.ifPresent(entry -> updated.extract(entry.entryId(), entry.stack().getCount()));
            if (existing.isPresent()) {
                save(updated);
            }
            return;
        }

        int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
        if (stack.getCount() > limit) {
            return;
        }
        ItemStack replacement = stack.copy();
        if (existing.isPresent()) {
            if (replaceEntry(updated, existing.get(), replacement)) {
                save(updated);
            }
            return;
        }
        int moved = GridExplicitInsertHelper.insertOrMergeAt(
                updated, replacement, slotX(updated, slot), slotY(updated, slot), false, 0);
        if (moved == replacement.getCount()) {
            save(updated);
        }
    }

    public synchronized boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && insertItem(slot, stack, true).getCount() < stack.getCount();
    }

    public synchronized ItemStack insertAutomatically(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            commitExternalMutations();
        }
        GridInventoryData updated = currentGrid().copy();
        ItemStack remainder = updated.insert(stack.copy(), GridInsertMode.EXECUTE);
        if (!simulate && remainder.getCount() < stack.getCount()) {
            save(updated);
        }
        return remainder;
    }

    public synchronized ItemStack extractMatching(ItemStack target, int amount, boolean simulate) {
        if (target.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            commitExternalMutations();
        }
        GridInventoryData updated = currentGrid().copy();
        ItemStack result = ItemStack.EMPTY;
        int remaining = amount;
        for (GridEntry entry : List.copyOf(updated.getEntries())) {
            if (!GridItemStacks.sameItemSameData(entry.stack(), target)) {
                continue;
            }
            ItemStack extracted = updated.extract(entry.entryId(), remaining);
            if (extracted.isEmpty()) {
                continue;
            }
            if (result.isEmpty()) {
                result = extracted;
            } else {
                result.grow(extracted.getCount());
            }
            remaining -= extracted.getCount();
            if (remaining <= 0) {
                break;
            }
        }
        if (!simulate && !result.isEmpty()) {
            save(updated);
        }
        return result;
    }

    public synchronized void accountSimpleStacks(StackedContents contents) {
        for (GridEntry entry : currentGrid().getEntries()) {
            contents.accountSimpleStack(entry.stack());
        }
    }

    public synchronized void commitExternalMutations() {
        if (borrowedStacks.isEmpty()) {
            return;
        }
        GridInventoryData updated = currentGrid().copy();
        boolean changed = false;
        for (BorrowedStack borrowed : borrowedStacks.values()) {
            if (ItemStack.matches(borrowed.original(), borrowed.exposed())) {
                continue;
            }
            Optional<GridEntry> current = updated.getEntry(borrowed.entryId());
            if (current.isEmpty()
                    || !ItemStack.matches(current.get().stack(), borrowed.original())
                    || borrowed.exposed().isEmpty()
                    || !borrowed.original().is(borrowed.exposed().getItem())
                    || borrowed.original().getCount() != borrowed.exposed().getCount()) {
                continue;
            }
            changed |= replaceEntry(updated, current.get(), borrowed.exposed());
        }
        borrowedStacks.clear();
        if (changed) {
            save(updated);
        }
    }

    public static void accountPlayerGrid(Player player, StackedContents contents) {
        if (isEnabled(player)) {
            new PlayerGridInventoryAccess(player).accountSimpleStacks(contents);
        }
    }

    public static ItemStack extractPlayerGridMatching(Player player, ItemStack target, int amount) {
        if (!isEnabled(player)) {
            return ItemStack.EMPTY;
        }
        return new PlayerGridInventoryAccess(player).extractMatching(target, amount, false);
    }

    public static boolean canReturnCraftingItems(Inventory inventory, List<ItemStack> stacks) {
        if (!isEnabled(inventory.player)) {
            return false;
        }
        List<ItemStack> main = inventory.items.stream().map(ItemStack::copy).collect(ArrayList::new, List::add, List::addAll);
        ItemStack offhand = inventory.offhand.get(0).copy();
        PlayerGridInventoryAccess gridAccess = new PlayerGridInventoryAccess(inventory.player);
        GridInventoryData simulatedGrid = gridAccess.currentGrid().copy();
        for (ItemStack source : stacks) {
            ItemStack remainder = simulateVanillaInsert(main, offhand, inventory.getMaxStackSize(), source.copy());
            if (!remainder.isEmpty()) {
                remainder = simulatedGrid.insert(remainder, GridInsertMode.EXECUTE);
            }
            if (!remainder.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void returnCraftingItem(Inventory inventory, ItemStack stack) {
        if (!isEnabled(inventory.player)) {
            inventory.placeItemBackInInventory(stack, false);
            return;
        }
        inventory.add(stack);
        if (!stack.isEmpty()) {
            ItemStack remainder = new PlayerGridInventoryAccess(inventory.player).insertAutomatically(stack, false);
            stack.setCount(remainder.getCount());
        }
        if (!stack.isEmpty()) {
            inventory.player.drop(stack, false);
        }
    }

    private GridInventoryData currentGrid() {
        int revision = player.getInventory().getTimesChanged();
        if (grid == null || loadedInventoryRevision != revision) {
            grid = PlayerPocketDefinitionManager.refreshShape(
                    GridInventoryServices.playerData().getPlayerGridInventory(player)).copy();
            loadedInventoryRevision = revision;
            borrowedStacks.clear();
        }
        return grid;
    }

    private void save(GridInventoryData updated) {
        grid = updated.copy();
        GridInventoryServices.playerData().setPlayerGridInventory(player, grid.copy());
        loadedInventoryRevision = player.getInventory().getTimesChanged();
        borrowedStacks.clear();
        if (player instanceof ServerPlayer serverPlayer) {
            GridInventoryServices.network().sendToPlayer(serverPlayer, new SyncExternalPlayerGridMessage(grid.copy()));
        }
    }

    private static boolean replaceEntry(GridInventoryData inventory, GridEntry current, ItemStack replacement) {
        if (!GridPlacementValidator.canPlace(
                inventory, replacement, current.x(), current.y(), current.rotated(), current.entryId(), 0)) {
            return false;
        }
        var size = GridItemSizeManager.getSize(replacement);
        for (int index = 0; index < inventory.getEntries().size(); index++) {
            if (inventory.getEntries().get(index).entryId().equals(current.entryId())) {
                inventory.getEntries().set(index, new GridEntry(
                        current.entryId(), replacement.copy(), current.x(), current.y(),
                        size.placedWidth(current.rotated()), size.placedHeight(current.rotated()), current.rotated()));
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    private static Optional<GridEntry> entryAtAnchor(GridInventoryData inventory, int slot) {
        if (!validSlot(inventory, slot)) {
            return Optional.empty();
        }
        int x = slotX(inventory, slot);
        int y = slotY(inventory, slot);
        return inventory.getEntries().stream().filter(entry -> entry.x() == x && entry.y() == y).findFirst();
    }

    private static boolean isCoveredNonAnchor(GridInventoryData inventory, int slot) {
        if (!validSlot(inventory, slot)) {
            return false;
        }
        int x = slotX(inventory, slot);
        int y = slotY(inventory, slot);
        return inventory.getEntries().stream()
                .anyMatch(entry -> entry.contains(x, y) && (entry.x() != x || entry.y() != y));
    }

    private static boolean validSlot(GridInventoryData inventory, int slot) {
        return slot >= 0 && slot < inventory.getColumns() * inventory.getRows();
    }

    private static int slotX(GridInventoryData inventory, int slot) {
        return slot % inventory.getColumns();
    }

    private static int slotY(GridInventoryData inventory, int slot) {
        return slot / inventory.getColumns();
    }

    private static ItemStack simulateVanillaInsert(List<ItemStack> main, ItemStack offhand, int inventoryLimit, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!stack.isDamaged()) {
            mergeInto(offhand, stack, inventoryLimit);
            for (ItemStack existing : main) {
                mergeInto(existing, stack, inventoryLimit);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        for (int index = 0; index < main.size() && !stack.isEmpty(); index++) {
            if (!main.get(index).isEmpty()) {
                continue;
            }
            int moved = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), inventoryLimit));
            main.set(index, stack.copyWithCount(moved));
            stack.shrink(moved);
        }
        return stack;
    }

    private static void mergeInto(ItemStack existing, ItemStack incoming, int inventoryLimit) {
        if (existing.isEmpty() || incoming.isEmpty() || !existing.isStackable()
                || !GridItemStacks.sameItemSameData(existing, incoming)) {
            return;
        }
        int limit = Math.min(existing.getMaxStackSize(), inventoryLimit);
        int moved = Math.min(incoming.getCount(), limit - existing.getCount());
        if (moved > 0) {
            existing.grow(moved);
            incoming.shrink(moved);
        }
    }

    private record BorrowedStack(UUID entryId, ItemStack original, ItemStack exposed) {
    }
}
