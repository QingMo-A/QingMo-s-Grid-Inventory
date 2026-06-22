package com.dreamingfish.gridinventory.platform.neoforge.compat;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.AccessorySlotView;
import com.dreamingfish.gridinventory.target.neoforge1211.registry.NeoForge1211DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NeoForgeCuriosIntegration {
    private static final ResourceLocation FALLBACK_ICON = ResourceLocation.fromNamespaceAndPath("curios", "slot/empty_curio_slot");

    private NeoForgeCuriosIntegration() {
    }

    public static boolean isLoaded() {
        return net.neoforged.fml.ModList.get().isLoaded("curios");
    }

    public static List<AccessorySlotView> collectSlots(Player player) {
        if (!isLoaded()) {
            return List.of();
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    List<AccessorySlotView> views = new ArrayList<>();
                    handler.getCurios().values().stream()
                            .filter(ICurioStacksHandler::isVisible)
                            .sorted(Comparator.comparing(ICurioStacksHandler::getIdentifier))
                            .forEach(stacksHandler -> {
                                IDynamicStackHandler stacks = stacksHandler.getStacks();
                                for (int index = 0; index < stacks.getSlots(); index++) {
                                    if (!handler.isSlotActive(stacksHandler.getIdentifier(), index)) {
                                        continue;
                                    }
                                    String identifier = stacksHandler.getIdentifier();
                                    ResourceLocation icon = CuriosApi.getSlot(identifier, player.level())
                                            .map(slot -> slot.getIcon())
                                            .orElse(FALLBACK_ICON);
                                    views.add(new AccessorySlotView(identifier, index, stacks.getStackInSlot(index).copy(), true, icon));
                                }
                            });
                    return views;
                })
                .orElse(List.of());
    }

    public static boolean movePlayerSlotToCurio(Player player, int sourcePlayerSlot, String identifier, int index) {
        if (!isLoaded() || sourcePlayerSlot < 0 || sourcePlayerSlot >= player.getInventory().getContainerSize()) {
            return false;
        }
        ItemStack source = player.getInventory().getItem(sourcePlayerSlot);
        if (source.isEmpty()) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty() || !validCurio(player, identifier, index, source)) {
            return false;
        }
        IDynamicStackHandler stacks = target.get();
        if (index < 0 || index >= stacks.getSlots()) {
            return false;
        }
        ItemStack existing = stacks.getStackInSlot(index);
        if (!existing.isEmpty()) {
            return false;
        }
        ItemStack moved = source.copyWithCount(1);
        GridBackpackItem.unfold(moved);
        stacks.setStackInSlot(index, moved);
        source.shrink(1);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return isLoaded() && targetEmpty && !stack.isEmpty() && validCurio(player, identifier, index, stack);
    }

    public static boolean moveGridEntryToCurio(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        if (!isLoaded()) {
            return false;
        }
        Optional<GridEntry> entry = grid.getEntry(entryId);
        if (entry.isEmpty() || !validCurio(player, identifier, index, entry.get().stack())) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty() || index < 0 || index >= target.get().getSlots() || !target.get().getStackInSlot(index).isEmpty()) {
            return false;
        }
        ItemStack moved = grid.extract(entryId, 1);
        if (moved.isEmpty()) {
            return false;
        }
        GridBackpackItem.unfold(moved);
        target.get().setStackInSlot(index, moved);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean moveEquipmentEntryToCurio(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        if (!isLoaded()) {
            return false;
        }
        Optional<GridEntry> entry = inventory.getEntry(entryId);
        if (entry.isEmpty() || !validCurio(player, identifier, index, entry.get().stack())) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty() || index < 0 || index >= target.get().getSlots() || !target.get().getStackInSlot(index).isEmpty()) {
            return false;
        }
        ItemStack moved = inventory.extract(entryId, 1);
        if (moved.isEmpty()) {
            return false;
        }
        GridBackpackItem.unfold(moved);
        target.get().setStackInSlot(index, moved);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean quickEquip(Player player, ItemStack source) {
        if (!isLoaded() || source.isEmpty()) {
            return false;
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    List<ICurioStacksHandler> handlers = handler.getCurios().values().stream()
                            .filter(ICurioStacksHandler::isVisible)
                            .sorted(Comparator.comparing(ICurioStacksHandler::getIdentifier))
                            .toList();
                    for (ICurioStacksHandler stacksHandler : handlers) {
                        IDynamicStackHandler stacks = stacksHandler.getStacks();
                        String identifier = stacksHandler.getIdentifier();
                        for (int index = 0; index < stacks.getSlots(); index++) {
                            if (!handler.isSlotActive(identifier, index) || !stacks.getStackInSlot(index).isEmpty()) {
                                continue;
                            }
                            if (validCurio(player, identifier, index, source)) {
                                ItemStack moved = source.copyWithCount(1);
                                GridBackpackItem.unfold(moved);
                                stacks.setStackInSlot(index, moved);
                                source.shrink(1);
                                player.getInventory().setChanged();
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    public static boolean insertStackIntoCurio(Player player, ItemStack source, String identifier, int index) {
        if (!isLoaded() || source.isEmpty() || !validCurio(player, identifier, index, source)) {
            return false;
        }
        Optional<IDynamicStackHandler> target = stacks(player, identifier);
        if (target.isEmpty() || index < 0 || index >= target.get().getSlots() || !target.get().getStackInSlot(index).isEmpty()) {
            return false;
        }
        ItemStack moved = source.copyWithCount(1);
        GridBackpackItem.unfold(moved);
        target.get().setStackInSlot(index, moved);
        source.shrink(1);
        player.getInventory().setChanged();
        return true;
    }

    public static boolean canQuickEquip(Player player, ItemStack stack) {
        if (!isLoaded() || stack.isEmpty()) {
            return false;
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.getCurios().values().stream()
                        .filter(ICurioStacksHandler::isVisible)
                        .anyMatch(stacksHandler -> {
                            IDynamicStackHandler stacks = stacksHandler.getStacks();
                            String identifier = stacksHandler.getIdentifier();
                            for (int index = 0; index < stacks.getSlots(); index++) {
                                if (handler.isSlotActive(identifier, index)
                                        && stacks.getStackInSlot(index).isEmpty()
                                        && validCurio(player, identifier, index, stack)) {
                                    return true;
                                }
                            }
                            return false;
                        }))
                .orElse(false);
    }

    public static boolean moveCurioToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        if (!isLoaded() || targetPlayerSlot < 0 || targetPlayerSlot >= player.getInventory().getContainerSize()) {
            return false;
        }
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return false;
        }
        ItemStack curio = source.get().getStackInSlot(index);
        ItemStack target = player.getInventory().getItem(targetPlayerSlot);
        if (curio.isEmpty() || !target.isEmpty()) {
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
        if (!isLoaded()) {
            return false;
        }
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
            if (targetFolded && !GridBackpackItem.canFold(moved)) {
                return false;
            }
            moved.set(NeoForge1211DataComponents.BACKPACK_FOLDED.get(), targetFolded);
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
        if (!isLoaded()) {
            return Optional.empty();
        }
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return Optional.empty();
        }
        ItemStack stack = source.get().getStackInSlot(index);
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack);
    }

    public static void setCurioStack(Player player, String identifier, int index, ItemStack stack) {
        if (!isLoaded()) {
            return;
        }
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return;
        }
        source.get().setStackInSlot(index, stack);
        player.getInventory().setChanged();
    }

    private static Optional<IDynamicStackHandler> stacks(Player player, String identifier) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(identifier))
                .map(ICurioStacksHandler::getStacks);
    }

    private static boolean validCurio(Player player, String identifier, int index, ItemStack stack) {
        return CuriosApi.isStackValid(new SlotContext(identifier, player, index, false, true), stack)
                && stacks(player, identifier).map(stacks -> index >= 0 && index < stacks.getSlots() && stacks.isItemValid(index, stack)).orElse(false);
    }
}
