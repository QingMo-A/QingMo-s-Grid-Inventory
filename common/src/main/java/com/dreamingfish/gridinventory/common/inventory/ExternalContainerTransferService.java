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

public final class ExternalContainerTransferService {
    private ExternalContainerTransferService() {
    }

    public static boolean supports(GridItemSource source, GridItemTarget target) {
        return target instanceof GridItemTarget.MenuSlot
                || source instanceof GridItemSource.MenuCarried
                || source instanceof GridItemSource.PlayerGridEntry
                && target instanceof GridItemTarget.PlayerGridPlacement;
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
        if (source instanceof GridItemSource.PlayerGridEntry entry
                && target instanceof GridItemTarget.PlayerGridPlacement placement) {
            return moveWithinPlayerGrid(player, entry, placement);
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
        int limit = playerSlot >= 36 ? 1 : Math.min(player.getInventory().getMaxStackSize(), moved.getMaxStackSize());
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

    private static boolean moveWithinPlayerGrid(ServerPlayer player, GridItemSource.PlayerGridEntry source,
                                                GridItemTarget.PlayerGridPlacement target) {
        GridInventoryData grid = playerGridCopy(player);
        Optional<GridEntry> entry = grid.getEntry(source.entryId());
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack prepared = prepareFolded(entry.get().stack(), target.folded());
        if (prepared.isEmpty()) {
            return false;
        }
        if (entry.get().stack().getItem() instanceof GridBackpackItem) {
            GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(),
                    GridBackpackItem.isFolded(prepared));
        }
        if (!grid.move(source.entryId(), target.x(), target.y(), target.rotated())) {
            return false;
        }
        savePlayerGrid(player, grid);
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

    private static boolean isFreePlayerSlot(int slot) {
        return slot >= 0 && slot <= 8 || slot >= 36 && slot <= 40;
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
