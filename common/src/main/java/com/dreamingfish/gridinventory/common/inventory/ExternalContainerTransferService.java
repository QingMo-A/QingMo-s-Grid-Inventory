package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.network.ModNetworking;
import com.dreamingfish.gridinventory.common.network.SyncExternalPlayerGridMessage;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class ExternalContainerTransferService {
    private ExternalContainerTransferService() {
    }

    public static boolean supports(GridItemSource source, GridItemTarget target) {
        if (target instanceof GridItemTarget.MenuSlot) {
            return isSidebarSource(source);
        }
        if (source instanceof GridItemSource.MenuCarried) {
            return isSidebarTarget(target);
        }
        return isSidebarSource(source) && isSidebarTarget(target);
    }

    public static boolean move(ServerPlayer player, AbstractContainerMenu menu, GridItemSource source,
                               GridItemTarget target, GridMoveOptions options) {
        GridMoveOptions safeOptions = options == null ? GridMoveOptions.all(false, false) : options;
        if (target instanceof GridItemTarget.MenuSlot menuSlot) {
            return moveToMenuSlot(player, menu, source, menuSlot, safeOptions);
        }
        if (source instanceof GridItemSource.MenuCarried carried) {
            return moveCarried(player, menu, carried, target, safeOptions);
        }
        if (isSidebarSource(source) && isSidebarTarget(target)) {
            boolean moved = moveWithinSidebar(player, source, target, safeOptions);
            if (moved) {
                player.getInventory().setChanged();
                menu.broadcastChanges();
            }
            return moved;
        }
        return false;
    }

    private static boolean moveWithinSidebar(ServerPlayer player, GridItemSource source, GridItemTarget target,
                                             GridMoveOptions options) {
        if (target instanceof GridItemTarget.PlayerGridPlacement placement) {
            return moveToPlayerGrid(player, source, placement, options);
        }
        if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            return moveToEquipmentStorage(player, source, placement, options);
        }
        if (target instanceof GridItemTarget.PlayerSlot slot) {
            return moveToPlayerSlot(player, source, slot.slot(), options);
        }
        if (target instanceof GridItemTarget.AccessorySlot slot) {
            return moveToAccessory(player, source, slot);
        }
        return false;
    }

    private static boolean moveToPlayerGrid(ServerPlayer player, GridItemSource source,
                                            GridItemTarget.PlayerGridPlacement target,
                                            GridMoveOptions options) {
        GridInventoryData grid = playerGridCopy(player);
        boolean moved;
        if (source instanceof GridItemSource.PlayerGridEntry entry) {
            moved = moveWithinGrid(grid, entry.entryId(), target.x(), target.y(), target.rotated(),
                    target.folded(), options.safeCount(), 0);
        } else {
            moved = moveSourceIntoGrid(player, source, grid, target.x(), target.y(), target.rotated(),
                    target.folded(), options.safeCount(), 0);
        }
        if (moved) {
            savePlayerGrid(player, grid);
        }
        return moved;
    }

    private static boolean moveToEquipmentStorage(ServerPlayer player, GridItemSource source,
                                                  GridItemTarget.EquipmentStoragePlacement target,
                                                  GridMoveOptions options) {
        if (source instanceof GridItemSource.PlayerSlot slot
                && equipmentPlayerSlotOrInvalid(target.slot()) == slot.slot()) {
            return false;
        }
        if (source instanceof GridItemSource.AccessorySlot slot
                && GridEquipmentSlots.isBack(target.slot())
                && "back".equals(slot.identifier()) && slot.index() == 0) {
            return false;
        }
        if (source instanceof GridItemSource.EquipmentStorageEntry entry
                && entry.slot() == target.slot()) {
            return moveWithinEquipmentStorage(player, entry, target, options.safeCount());
        }
        Optional<EquipmentStorageEdit> edit = equipmentStorage(player, target.slot(), target.containerId());
        if (edit.isEmpty()) {
            return false;
        }
        boolean moved = moveSourceIntoGrid(player, source, edit.get().inventory(), target.x(), target.y(),
                target.rotated(), target.folded(), options.safeCount(), 1);
        if (moved) {
            saveEquipmentStorage(player, target.slot(), edit.get().storage());
        }
        return moved;
    }

    private static boolean moveWithinEquipmentStorage(ServerPlayer player,
                                                      GridItemSource.EquipmentStorageEntry source,
                                                      GridItemTarget.EquipmentStoragePlacement target,
                                                      int requested) {
        Optional<EquipmentStorageEdit> edit = equipmentStorage(player, target.slot(), target.containerId());
        if (edit.isEmpty()) {
            return false;
        }
        Optional<GridInventoryData> sourceInventory = edit.get().storage().containers().stream()
                .filter(container -> container.id().equals(source.containerId()))
                .map(NamedGridInventoryData::inventory)
                .findFirst();
        if (sourceInventory.isEmpty()) {
            return false;
        }
        boolean moved = sourceInventory.get() == edit.get().inventory()
                ? moveWithinGrid(sourceInventory.get(), source.entryId(), target.x(), target.y(), target.rotated(),
                target.folded(), requested, 1)
                : moveBetweenGrids(sourceInventory.get(), source.entryId(), edit.get().inventory(),
                target.x(), target.y(), target.rotated(), target.folded(), requested, 1);
        if (moved) {
            saveEquipmentStorage(player, target.slot(), edit.get().storage());
        }
        return moved;
    }

    private static boolean moveToPlayerSlot(ServerPlayer player, GridItemSource source, int targetSlot,
                                            GridMoveOptions options) {
        if (source instanceof GridItemSource.PlayerSlot playerSlot) {
            return moveBetweenPlayerSlots(player, playerSlot.slot(), targetSlot, options.targetFolded());
        }
        if (!isFreePlayerSlot(targetSlot)) {
            return false;
        }
        Optional<SourceHandle> resolved = resolveSource(player, source);
        if (resolved.isEmpty()) {
            return false;
        }
        ItemStack moved = prepareFolded(resolved.get().stack().copyWithCount(
                Math.min(resolved.get().stack().getCount(), options.safeCount())), options.targetFolded());
        if (moved.isEmpty() || !mayInsertIntoPlayerSlot(player, targetSlot, moved)) {
            return false;
        }
        ItemStack existing = player.getInventory().getItem(targetSlot);
        int limit = playerSlotLimit(player, targetSlot, moved);
        int accepted;
        if (existing.isEmpty()) {
            accepted = Math.min(moved.getCount(), limit);
        } else if (GridItemStacks.sameItemSameData(existing, moved)) {
            accepted = Math.min(moved.getCount(), Math.max(0, limit - existing.getCount()));
        } else {
            return false;
        }
        if (accepted <= 0 || !resolved.get().remove(accepted)) {
            return false;
        }
        if (existing.isEmpty()) {
            player.getInventory().setItem(targetSlot, moved.copyWithCount(accepted));
        } else {
            existing.grow(accepted);
        }
        syncPlayerSlot(player, targetSlot);
        return true;
    }

    private static boolean moveToAccessory(ServerPlayer player, GridItemSource source,
                                           GridItemTarget.AccessorySlot target) {
        if (source instanceof GridItemSource.PlayerSlot slot) {
            return isFreePlayerSlot(slot.slot()) && GridInventoryServices.accessories()
                    .movePlayerSlotToAccessory(player, slot.slot(), target.identifier(), target.index());
        }
        if (source instanceof GridItemSource.PlayerGridEntry entry) {
            GridInventoryData grid = playerGridCopy(player);
            boolean moved = GridInventoryServices.accessories().moveGridEntryToAccessory(
                    player, grid, entry.entryId(), target.identifier(), target.index());
            if (moved) {
                savePlayerGrid(player, grid);
            }
            return moved;
        }
        if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
            Optional<EquipmentStorageEdit> edit = equipmentStorage(player, entry.slot(), entry.containerId());
            if (edit.isEmpty()) {
                return false;
            }
            boolean moved = GridInventoryServices.accessories().moveEquipmentEntryToAccessory(
                    player, edit.get().inventory(), entry.entryId(), target.identifier(), target.index());
            if (moved) {
                saveEquipmentStorage(player, entry.slot(), edit.get().storage());
            }
            return moved;
        }
        if (source instanceof GridItemSource.AccessorySlot slot) {
            return moveBetweenAccessories(player, slot, target);
        }
        return false;
    }

    private static boolean moveToMenuSlot(ServerPlayer player, AbstractContainerMenu menu, GridItemSource source,
                                          GridItemTarget.MenuSlot target, GridMoveOptions options) {
        if (target.containerId() != menu.containerId || target.slotIndex() < 0
                || target.slotIndex() >= menu.slots.size()) {
            return false;
        }
        Slot slot = menu.slots.get(target.slotIndex());
        if (slot.container == player.getInventory() || !menu.getCarried().isEmpty()) {
            return false;
        }
        Optional<SourceHandle> resolved = resolveSource(player, source);
        if (resolved.isEmpty()) {
            return false;
        }
        ItemStack moved = prepareFolded(resolved.get().stack().copyWithCount(
                Math.min(resolved.get().stack().getCount(), options.safeCount())), options.targetFolded());
        if (moved.isEmpty() || !slot.mayPlace(moved)) {
            return false;
        }
        ItemStack originalSlot = slot.getItem().copy();
        if (!originalSlot.isEmpty() && !GridItemStacks.sameItemSameData(originalSlot, moved)) {
            return false;
        }
        ItemStack originalCarried = menu.getCarried().copy();
        menu.setCarried(moved.copy());
        menu.clicked(target.slotIndex(), 0, ClickType.PICKUP, player);
        ItemStack remainder = menu.getCarried();
        int accepted = remainder.isEmpty() ? moved.getCount()
                : GridItemStacks.sameItemSameData(remainder, moved)
                ? moved.getCount() - remainder.getCount() : -1;
        if (accepted <= 0 || accepted > moved.getCount() || !resolved.get().remove(accepted)) {
            slot.set(originalSlot);
            slot.setChanged();
            menu.setCarried(originalCarried);
            menu.broadcastChanges();
            return false;
        }
        menu.setCarried(originalCarried);
        menu.broadcastChanges();
        return true;
    }

    private static boolean moveCarried(ServerPlayer player, AbstractContainerMenu menu,
                                       GridItemSource.MenuCarried source, GridItemTarget target,
                                       GridMoveOptions options) {
        if (source.containerId() != menu.containerId || menu.getCarried().isEmpty()) {
            return false;
        }
        boolean moved;
        if (target instanceof GridItemTarget.PlayerGridPlacement placement) {
            moved = insertCarriedIntoPlayerGrid(player, menu, placement, options.safeCount());
        } else if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            moved = insertCarriedIntoEquipmentStorage(player, menu, placement, options.safeCount());
        } else if (target instanceof GridItemTarget.PlayerSlot slot) {
            moved = insertCarriedIntoPlayerSlot(player, menu, slot.slot(), options.targetFolded(), options.safeCount());
        } else if (target instanceof GridItemTarget.AccessorySlot slot) {
            moved = GridInventoryServices.accessories().insertStackIntoAccessory(
                    player, menu.getCarried(), slot.identifier(), slot.index());
        } else {
            moved = false;
        }
        if (moved) {
            if (menu.getCarried().isEmpty()) {
                menu.setCarried(ItemStack.EMPTY);
            }
            player.getInventory().setChanged();
            menu.broadcastChanges();
        }
        return moved;
    }

    private static boolean insertCarriedIntoPlayerGrid(ServerPlayer player, AbstractContainerMenu menu,
                                                        GridItemTarget.PlayerGridPlacement target, int requested) {
        ItemStack carried = menu.getCarried();
        int amount = Math.min(carried.getCount(), requested);
        ItemStack moved = prepareFolded(carried.copyWithCount(amount), target.folded());
        if (moved.isEmpty()) {
            return false;
        }
        GridInventoryData grid = playerGridCopy(player);
        int inserted = GridExplicitInsertHelper.insertOrMergeAt(grid, moved, target.x(), target.y(),
                target.rotated(), 0);
        if (inserted <= 0) {
            return false;
        }
        carried.shrink(inserted);
        savePlayerGrid(player, grid);
        return true;
    }

    private static boolean insertCarriedIntoEquipmentStorage(ServerPlayer player, AbstractContainerMenu menu,
                                                              GridItemTarget.EquipmentStoragePlacement target,
                                                              int requested) {
        Optional<EquipmentStorageEdit> edit = equipmentStorage(player, target.slot(), target.containerId());
        if (edit.isEmpty()) {
            return false;
        }
        ItemStack carried = menu.getCarried();
        int amount = Math.min(carried.getCount(), requested);
        ItemStack moved = prepareFolded(carried.copyWithCount(amount), target.folded());
        if (moved.isEmpty()) {
            return false;
        }
        int inserted = GridExplicitInsertHelper.insertOrMergeAt(edit.get().inventory(), moved,
                target.x(), target.y(), target.rotated(), 1);
        if (inserted <= 0) {
            return false;
        }
        carried.shrink(inserted);
        saveEquipmentStorage(player, target.slot(), edit.get().storage());
        return true;
    }

    private static boolean insertCarriedIntoPlayerSlot(ServerPlayer player, AbstractContainerMenu menu,
                                                        int playerSlot, boolean folded, int requested) {
        if (!isFreePlayerSlot(playerSlot)) {
            return false;
        }
        ItemStack carried = menu.getCarried();
        ItemStack moved = prepareFolded(carried.copyWithCount(Math.min(carried.getCount(), requested)), folded);
        if (moved.isEmpty() || !mayInsertIntoPlayerSlot(player, playerSlot, moved)) {
            return false;
        }
        ItemStack existing = player.getInventory().getItem(playerSlot);
        int limit = playerSlotLimit(player, playerSlot, moved);
        int accepted;
        if (existing.isEmpty()) {
            accepted = Math.min(moved.getCount(), limit);
            player.getInventory().setItem(playerSlot, moved.copyWithCount(accepted));
        } else if (GridItemStacks.sameItemSameData(existing, moved)) {
            accepted = Math.min(moved.getCount(), Math.max(0, limit - existing.getCount()));
            if (accepted <= 0) {
                return false;
            }
            existing.grow(accepted);
        } else {
            return false;
        }
        carried.shrink(accepted);
        player.getInventory().setChanged();
        syncPlayerSlot(player, playerSlot);
        return accepted > 0;
    }

    private static boolean moveSourceIntoGrid(ServerPlayer player, GridItemSource source,
                                              GridInventoryData targetInventory, int targetX, int targetY,
                                              boolean rotated, boolean folded, int requested, int targetDepth) {
        Optional<SourceHandle> resolved = resolveSource(player, source);
        if (resolved.isEmpty()) {
            return false;
        }
        int amount = Math.min(resolved.get().stack().getCount(), requested);
        if (source instanceof GridItemSource.PlayerSlot && !GridStackMerger.itemsStackableInGrid()) {
            amount = Math.min(amount, 1);
        }
        ItemStack moved = prepareFolded(resolved.get().stack().copyWithCount(amount), folded);
        if (moved.isEmpty()) {
            return false;
        }
        int inserted = GridExplicitInsertHelper.insertOrMergeAt(targetInventory, moved, targetX, targetY,
                rotated, targetDepth);
        return inserted > 0 && resolved.get().remove(inserted);
    }

    private static boolean moveWithinGrid(GridInventoryData inventory, UUID sourceEntryId,
                                          int targetX, int targetY, boolean rotated, boolean folded,
                                          int requested, int targetDepth) {
        Optional<GridEntry> source = inventory.getEntry(sourceEntryId);
        if (source.isEmpty()) {
            return false;
        }
        int amount = Math.min(source.get().stack().getCount(), requested);
        ItemStack moved = prepareFolded(source.get().stack().copyWithCount(amount), folded);
        if (moved.isEmpty()) {
            return false;
        }
        Optional<GridEntry> destination = inventory.getEntries().stream()
                .filter(entry -> !entry.entryId().equals(sourceEntryId))
                .filter(entry -> entry.contains(targetX, targetY))
                .findFirst();
        if (destination.isPresent()) {
            ItemStack targetStack = destination.get().stack();
            if (!GridStackMerger.itemsStackableInGrid()
                    || !GridItemStacks.sameItemSameData(targetStack, moved)
                    || targetStack.getCount() >= targetStack.getMaxStackSize()) {
                return false;
            }
            int accepted = Math.min(amount, targetStack.getMaxStackSize() - targetStack.getCount());
            ItemStack removed = inventory.extract(sourceEntryId, accepted);
            Optional<GridEntry> updatedDestination = inventory.getEntry(destination.get().entryId());
            if (removed.getCount() != accepted || updatedDestination.isEmpty()) {
                return false;
            }
            updatedDestination.get().stack().grow(accepted);
            inventory.setChanged();
            return true;
        }
        if (amount == source.get().stack().getCount()) {
            if (!GridPlacementValidator.canPlace(inventory, moved, targetX, targetY, rotated,
                    sourceEntryId, targetDepth)) {
                return false;
            }
            if (source.get().stack().getItem() instanceof GridBackpackItem) {
                GridInventoryServices.itemStackData().setBackpackFolded(source.get().stack(),
                        GridBackpackItem.isFolded(moved));
            }
            return inventory.move(sourceEntryId, targetX, targetY, rotated);
        }
        ItemStack removed = inventory.extract(sourceEntryId, amount);
        if (removed.getCount() != amount) {
            return false;
        }
        int inserted = GridExplicitInsertHelper.insertOrMergeAt(inventory, moved, targetX, targetY,
                rotated, targetDepth);
        return inserted == amount;
    }

    private static boolean moveBetweenGrids(GridInventoryData sourceInventory, UUID sourceEntryId,
                                            GridInventoryData targetInventory, int targetX, int targetY,
                                            boolean rotated, boolean folded, int requested, int targetDepth) {
        Optional<GridEntry> source = sourceInventory.getEntry(sourceEntryId);
        if (source.isEmpty()) {
            return false;
        }
        int amount = Math.min(source.get().stack().getCount(), requested);
        ItemStack moved = prepareFolded(source.get().stack().copyWithCount(amount), folded);
        if (moved.isEmpty()) {
            return false;
        }
        int inserted = GridExplicitInsertHelper.insertOrMergeAt(targetInventory, moved, targetX, targetY,
                rotated, targetDepth);
        if (inserted <= 0) {
            return false;
        }
        ItemStack removed = sourceInventory.extract(sourceEntryId, inserted);
        return removed.getCount() == inserted;
    }

    private static boolean moveBetweenPlayerSlots(ServerPlayer player, int sourceSlot, int targetSlot,
                                                  boolean targetFolded) {
        if (!isFreePlayerSlot(sourceSlot) || !isFreePlayerSlot(targetSlot)) {
            return false;
        }
        ItemStack source = player.getInventory().getItem(sourceSlot);
        if (source.isEmpty() || !mayInsertIntoPlayerSlot(player, targetSlot, source)) {
            return false;
        }
        ItemStack prepared = prepareFolded(source.copy(), targetFolded);
        if (prepared.isEmpty()) {
            return false;
        }
        if (sourceSlot == targetSlot) {
            player.getInventory().setItem(sourceSlot, prepared);
            syncPlayerSlot(player, sourceSlot);
            return true;
        }
        ItemStack target = player.getInventory().getItem(targetSlot);
        int targetLimit = playerSlotLimit(player, targetSlot, prepared);
        if (target.isEmpty()) {
            int accepted = Math.min(prepared.getCount(), targetLimit);
            player.getInventory().setItem(targetSlot, prepared.copyWithCount(accepted));
            source.shrink(accepted);
        } else if (GridItemStacks.sameItemSameData(target, prepared)) {
            int accepted = Math.min(source.getCount(), Math.max(0, targetLimit - target.getCount()));
            if (accepted <= 0) {
                return false;
            }
            target.grow(accepted);
            source.shrink(accepted);
        } else {
            int sourceLimit = playerSlotLimit(player, sourceSlot, target);
            if (!mayInsertIntoPlayerSlot(player, sourceSlot, target)
                    || prepared.getCount() > targetLimit || target.getCount() > sourceLimit) {
                return false;
            }
            player.getInventory().setItem(sourceSlot, target.copy());
            player.getInventory().setItem(targetSlot, prepared);
        }
        syncPlayerSlot(player, sourceSlot);
        syncPlayerSlot(player, targetSlot);
        return true;
    }

    private static boolean moveBetweenAccessories(ServerPlayer player, GridItemSource.AccessorySlot source,
                                                  GridItemTarget.AccessorySlot target) {
        if (source.identifier().equals(target.identifier()) && source.index() == target.index()) {
            return false;
        }
        Optional<ItemStack> sourceStack = GridInventoryServices.accessories()
                .getAccessoryStack(player, source.identifier(), source.index());
        Optional<ItemStack> targetStack = GridInventoryServices.accessories()
                .getAccessoryStack(player, target.identifier(), target.index());
        if (sourceStack.isEmpty() || sourceStack.get().isEmpty()
                || targetStack.isPresent() && !targetStack.get().isEmpty()) {
            return false;
        }
        ItemStack remainder = sourceStack.get().copy();
        if (!GridInventoryServices.accessories().insertStackIntoAccessory(
                player, remainder, target.identifier(), target.index())) {
            return false;
        }
        Optional<ItemStack> currentSource = GridInventoryServices.accessories()
                .getAccessoryStack(player, source.identifier(), source.index());
        if (currentSource.isEmpty()
                || !GridItemStacks.sameItemSameData(currentSource.get(), sourceStack.get())
                || currentSource.get().getCount() != sourceStack.get().getCount()) {
            GridInventoryServices.accessories().setAccessoryStack(
                    player, target.identifier(), target.index(), ItemStack.EMPTY);
            return false;
        }
        GridInventoryServices.accessories().setAccessoryStack(
                player, source.identifier(), source.index(), remainder);
        return true;
    }

    private static Optional<SourceHandle> resolveSource(ServerPlayer player, GridItemSource source) {
        if (source instanceof GridItemSource.PlayerGridEntry entry) {
            GridInventoryData grid = playerGridCopy(player);
            Optional<GridEntry> found = grid.getEntry(entry.entryId());
            return found.map(value -> new SourceHandle(value.stack().copy(), amount -> {
                ItemStack removed = grid.extract(entry.entryId(), amount);
                if (removed.getCount() != amount) {
                    return false;
                }
                savePlayerGrid(player, grid);
                return true;
            }));
        }
        if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
            Optional<EquipmentStorageEdit> edit = equipmentStorage(player, entry.slot(), entry.containerId());
            if (edit.isEmpty()) {
                return Optional.empty();
            }
            Optional<GridEntry> found = edit.get().inventory().getEntry(entry.entryId());
            return found.map(value -> new SourceHandle(value.stack().copy(), amount -> {
                ItemStack removed = edit.get().inventory().extract(entry.entryId(), amount);
                if (removed.getCount() != amount) {
                    return false;
                }
                saveEquipmentStorage(player, entry.slot(), edit.get().storage());
                return true;
            }));
        }
        if (source instanceof GridItemSource.PlayerSlot slot && isFreePlayerSlot(slot.slot())) {
            ItemStack stack = player.getInventory().getItem(slot.slot());
            if (stack.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new SourceHandle(stack.copy(), amount -> {
                ItemStack current = player.getInventory().getItem(slot.slot());
                if (current.isEmpty() || current.getCount() < amount
                        || !GridItemStacks.sameItemSameData(current, stack)) {
                    return false;
                }
                current.shrink(amount);
                player.getInventory().setChanged();
                syncPlayerSlot(player, slot.slot());
                return true;
            }));
        }
        if (source instanceof GridItemSource.AccessorySlot slot) {
            Optional<ItemStack> stack = GridInventoryServices.accessories()
                    .getAccessoryStack(player, slot.identifier(), slot.index());
            if (stack.isEmpty() || stack.get().isEmpty()) {
                return Optional.empty();
            }
            ItemStack snapshot = stack.get().copy();
            return Optional.of(new SourceHandle(snapshot, amount -> {
                Optional<ItemStack> current = GridInventoryServices.accessories()
                        .getAccessoryStack(player, slot.identifier(), slot.index());
                if (current.isEmpty() || current.get().getCount() < amount
                        || !GridItemStacks.sameItemSameData(current.get(), snapshot)) {
                    return false;
                }
                ItemStack remainder = current.get().copy();
                remainder.shrink(amount);
                GridInventoryServices.accessories().setAccessoryStack(player, slot.identifier(), slot.index(), remainder);
                player.getInventory().setChanged();
                return true;
            }));
        }
        return Optional.empty();
    }

    private static GridInventoryData playerGridCopy(ServerPlayer player) {
        return PlayerPocketDefinitionManager.refreshShape(
                GridInventoryServices.playerData().getPlayerGridInventory(player)).copy();
    }

    private static void savePlayerGrid(ServerPlayer player, GridInventoryData grid) {
        GridInventoryServices.playerData().setPlayerGridInventory(player, grid.copy());
        GridInventoryServices.network().sendToPlayer(player, new SyncExternalPlayerGridMessage(grid.copy()));
    }

    private static Optional<EquipmentStorageEdit> equipmentStorage(ServerPlayer player, EquipmentSlot slot,
                                                                   String containerId) {
        ItemStack equipped = equipmentStorageStack(player, slot);
        EquipmentStorageData current = GridInventoryServices.itemStackData().getEquipmentStorage(equipped);
        if ((current == null || current.containers().isEmpty()) && !equipped.isEmpty()) {
            current = EquipmentStorageManager.initializeStorage(equipped, slot);
        }
        if (current == null) {
            return Optional.empty();
        }
        EquipmentStorageData copy = new EquipmentStorageData(current.containers().stream()
                .map(container -> new NamedGridInventoryData(container.id(), container.title(),
                        container.inventory().copy()))
                .toList());
        return copy.containers().stream()
                .filter(container -> container.id().equals(containerId))
                .map(container -> new EquipmentStorageEdit(copy, container.inventory()))
                .findFirst();
    }

    private static void saveEquipmentStorage(ServerPlayer player, EquipmentSlot slot, EquipmentStorageData storage) {
        ItemStack equipped = equipmentStorageStack(player, slot);
        if (equipped.isEmpty()) {
            return;
        }
        GridInventoryServices.itemStackData().setEquipmentStorage(equipped, storage);
        if (GridEquipmentSlots.isBack(slot)) {
            GridInventoryServices.accessories().setAccessoryStack(player, "back", 0, equipped);
        }
        player.getInventory().setChanged();
        ModNetworking.syncEquipmentStorage(player, slot, storage);
    }

    private static ItemStack equipmentStorageStack(ServerPlayer player, EquipmentSlot slot) {
        if (GridEquipmentSlots.isBack(slot)) {
            return GridInventoryServices.accessories().getAccessoryStack(player, "back", 0).orElse(ItemStack.EMPTY);
        }
        return player.getItemBySlot(slot);
    }

    private static ItemStack prepareFolded(ItemStack stack, boolean folded) {
        if (!(stack.getItem() instanceof GridBackpackItem)) {
            return stack;
        }
        if (folded && !GridBackpackItem.canFold(stack)) {
            return ItemStack.EMPTY;
        }
        GridInventoryServices.itemStackData().setBackpackFolded(stack, folded);
        return stack;
    }

    private static boolean isSidebarSource(GridItemSource source) {
        return source instanceof GridItemSource.PlayerGridEntry
                || source instanceof GridItemSource.EquipmentStorageEntry
                || source instanceof GridItemSource.PlayerSlot
                || source instanceof GridItemSource.AccessorySlot;
    }

    private static boolean isSidebarTarget(GridItemTarget target) {
        return target instanceof GridItemTarget.PlayerGridPlacement
                || target instanceof GridItemTarget.EquipmentStoragePlacement
                || target instanceof GridItemTarget.PlayerSlot
                || target instanceof GridItemTarget.AccessorySlot;
    }

    private static boolean isFreePlayerSlot(int slot) {
        return slot >= 0 && slot <= 8 || slot >= 36 && slot <= 40;
    }

    private static int playerSlotLimit(ServerPlayer player, int slot, ItemStack stack) {
        int limit = Math.min(player.getInventory().getMaxStackSize(), stack.getMaxStackSize());
        return slot >= 36 && slot <= 39 ? Math.min(limit, 1) : limit;
    }

    private static int equipmentPlayerSlotOrInvalid(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 39;
            case CHEST -> 38;
            case LEGS -> 37;
            case FEET -> 36;
            default -> -1;
        };
    }

    private static boolean mayInsertIntoPlayerSlot(ServerPlayer player, int slot, ItemStack stack) {
        return switch (slot) {
            case 39 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.HEAD, player);
            case 38 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.CHEST, player);
            case 37 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.LEGS, player);
            case 36 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.FEET, player);
            default -> true;
        };
    }

    private static void syncPlayerSlot(ServerPlayer player, int slot) {
        player.connection.send(new ClientboundContainerSetSlotPacket(
                ClientboundContainerSetSlotPacket.PLAYER_INVENTORY, 0, slot,
                player.getInventory().getItem(slot).copy()));
    }

    private record SourceHandle(ItemStack stack, RemoveOperation removeOperation) {
        boolean remove(int amount) {
            return removeOperation.remove(amount);
        }
    }

    @FunctionalInterface
    private interface RemoveOperation {
        boolean remove(int amount);
    }

    private record EquipmentStorageEdit(EquipmentStorageData storage, GridInventoryData inventory) {
    }
}
