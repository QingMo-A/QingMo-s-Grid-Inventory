package com.dreamingfish.gridinventory.target.forge1201.compat.curios;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.AccessorySlotView;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Forge1201CuriosIntegration {
    private static final ResourceLocation FALLBACK_ICON = new ResourceLocation("curios", "slot/empty_curio_slot");

    private Forge1201CuriosIntegration() {
    }

    public static List<AccessorySlotView> collectSlots(Player player) {
        Optional<ICuriosItemHandler> handler = handler(player);
        if (handler.isEmpty()) {
            return List.of();
        }
        List<AccessorySlotView> views = new ArrayList<>();
        handler.get().getCurios().values().stream()
                .filter(ICurioStacksHandler::isVisible)
                .sorted(Comparator.comparing(ICurioStacksHandler::getIdentifier))
                .forEach(stacksHandler -> {
                    IDynamicStackHandler stacks = stacksHandler.getStacks();
                    String identifier = stacksHandler.getIdentifier();
                    for (int index = 0; index < stacks.getSlots(); index++) {
                        ResourceLocation icon = CuriosApi.getSlot(identifier, player.level())
                                .map(slot -> slot.getIcon())
                                .orElse(FALLBACK_ICON);
                        views.add(new AccessorySlotView(identifier, index, stacks.getStackInSlot(index).copy(), true, icon));
                    }
                });
        return views;
    }

    public static boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return targetEmpty && validCurio(player, identifier, index, stack);
    }

    public static boolean canQuickEquip(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Optional<ICuriosItemHandler> handler = handler(player);
        if (handler.isEmpty()) {
            return false;
        }
        List<ICurioStacksHandler> handlers = sortedVisibleHandlers(handler.get());
        for (ICurioStacksHandler stacksHandler : handlers) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            String identifier = stacksHandler.getIdentifier();
            for (int index = 0; index < stacks.getSlots(); index++) {
                if (stacks.getStackInSlot(index).isEmpty()
                        && validCurio(player, identifier, index, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean quickEquip(Player player, ItemStack source) {
        if (source.isEmpty()) {
            return false;
        }
        Optional<ICuriosItemHandler> handler = handler(player);
        if (handler.isEmpty()) {
            return false;
        }
        List<ICurioStacksHandler> handlers = sortedVisibleHandlers(handler.get());
        for (ICurioStacksHandler stacksHandler : handlers) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            String identifier = stacksHandler.getIdentifier();
            for (int index = 0; index < stacks.getSlots(); index++) {
                if (!stacks.getStackInSlot(index).isEmpty()) {
                    continue;
                }
                if (!validCurio(player, identifier, index, source)) {
                    continue;
                }
                ItemStack moved = source.copyWithCount(1);
                if (moved.getItem() instanceof GridBackpackItem) {
                    GridBackpackItem.unfold(moved);
                }
                stacks.setStackInSlot(index, moved);
                source.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    public static boolean movePlayerSlotToCurio(Player player, int sourcePlayerSlot, String identifier, int index) {
        if (sourcePlayerSlot < 0 || sourcePlayerSlot >= player.getInventory().getContainerSize()) {
            return false;
        }
        ItemStack source = player.getInventory().getItem(sourcePlayerSlot);
        if (source.isEmpty() || !targetSlotEmptyAndValid(player, identifier, index, source)) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty()) {
            return false;
        }
        ItemStack moved = source.copyWithCount(1);
        if (moved.getItem() instanceof GridBackpackItem) {
            GridBackpackItem.unfold(moved);
        }
        target.get().setStackInSlot(index, moved);
        source.shrink(1);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean moveGridEntryToCurio(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        Optional<GridEntry> entry = grid.getEntry(entryId);
        if (entry.isEmpty() || !targetSlotEmptyAndValid(player, identifier, index, entry.get().stack())) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty()) {
            return false;
        }
        ItemStack moved = grid.extract(entryId, 1);
        if (moved.isEmpty()) {
            return false;
        }
        if (moved.getItem() instanceof GridBackpackItem) {
            GridBackpackItem.unfold(moved);
        }
        target.get().setStackInSlot(index, moved);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean moveEquipmentEntryToCurio(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        Optional<GridEntry> entry = inventory.getEntry(entryId);
        if (entry.isEmpty() || !targetSlotEmptyAndValid(player, identifier, index, entry.get().stack())) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty()) {
            return false;
        }
        ItemStack moved = inventory.extract(entryId, 1);
        if (moved.isEmpty()) {
            return false;
        }
        if (moved.getItem() instanceof GridBackpackItem) {
            GridBackpackItem.unfold(moved);
        }
        target.get().setStackInSlot(index, moved);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean moveCurioToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        if (targetPlayerSlot < 0 || targetPlayerSlot >= player.getInventory().getContainerSize()) {
            return false;
        }
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return false;
        }
        ItemStack curio = source.get().getStackInSlot(index);
        if (curio.isEmpty() || !player.getInventory().getItem(targetPlayerSlot).isEmpty()) {
            return false;
        }
        player.getInventory().setItem(targetPlayerSlot, curio.copy());
        source.get().setStackInSlot(index, ItemStack.EMPTY);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
        return moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated, false);
    }

    public static boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return false;
        }
        ItemStack curio = source.get().getStackInSlot(index);
        if (curio.isEmpty()) {
            return false;
        }
        ItemStack moved = curio.copy();
        if (moved.getItem() instanceof GridBackpackItem) {
            if (targetFolded) {
                if (!GridBackpackItem.canFold(moved)) {
                    return false;
                }
                GridInventoryServices.itemStackData().setBackpackFolded(moved, true);
            } else {
                GridBackpackItem.unfold(moved);
            }
        }
        if (!GridPlacementValidator.canPlace(grid, moved, targetX, targetY, rotated, null)) {
            return false;
        }
        grid.add(moved, targetX, targetY, rotated);
        source.get().setStackInSlot(index, ItemStack.EMPTY);
        player.getInventory().setChanged();
        return true;
    }

    public static Optional<ItemStack> getCurioStack(Player player, String identifier, int index) {
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return Optional.empty();
        }
        ItemStack stack = source.get().getStackInSlot(index);
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
    }

    public static void setCurioStack(Player player, String identifier, int index, ItemStack stack) {
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return;
        }
        source.get().setStackInSlot(index, stack);
        player.getInventory().setChanged();
    }

    private static Optional<ICuriosItemHandler> handler(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve();
    }

    private static Optional<IDynamicStackHandler> stacks(Player player, String identifier) {
        return handler(player)
                .flatMap(curios -> curios.getStacksHandler(identifier))
                .map(ICurioStacksHandler::getStacks);
    }

    private static boolean targetSlotEmptyAndValid(Player player, String identifier, int index, ItemStack stack) {
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        return target.isPresent()
                && index >= 0
                && index < target.get().getSlots()
                && target.get().getStackInSlot(index).isEmpty()
                && validCurio(player, identifier, index, stack);
    }

    private static boolean validCurio(Player player, String identifier, int index, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Optional<ICuriosItemHandler> handler = handler(player);
        Optional<IDynamicStackHandler> stacks = stacks(player, identifier);
        return handler.isPresent()
                && stacks.isPresent()
                && index >= 0
                && index < stacks.get().getSlots()
                && CuriosApi.isStackValid(new SlotContext(identifier, player, index, false, true), stack)
                && stacks.get().isItemValid(index, stack);
    }

    private static List<ICurioStacksHandler> sortedVisibleHandlers(ICuriosItemHandler handler) {
        return handler.getCurios().values().stream()
                .filter(ICurioStacksHandler::isVisible)
                .sorted(Comparator.comparing(ICurioStacksHandler::getIdentifier))
                .toList();
    }
}
