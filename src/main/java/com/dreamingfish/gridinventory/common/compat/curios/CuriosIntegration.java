package com.dreamingfish.gridinventory.common.compat.curios;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CuriosIntegration {
    private static final ResourceLocation FALLBACK_ICON = ResourceLocation.fromNamespaceAndPath("curios", "slot/empty_curio_slot");

    private CuriosIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    public static List<CuriosSlotView> collectSlots(Player player) {
        if (!isLoaded()) {
            return List.of();
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    List<CuriosSlotView> views = new ArrayList<>();
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
                                    views.add(new CuriosSlotView(identifier, index, stacks.getStackInSlot(index).copy(), true, icon));
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
        stacks.setStackInSlot(index, source.copyWithCount(1));
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
        target.get().setStackInSlot(index, moved);
        player.getInventory().setChanged();
        return true;
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
        if (!isLoaded()) {
            return false;
        }
        Optional<IDynamicStackHandler> source = stacks(player, identifier);
        if (source.isEmpty() || index < 0 || index >= source.get().getSlots()) {
            return false;
        }
        ItemStack curio = source.get().getStackInSlot(index);
        if (curio.isEmpty() || !GridPlacementValidator.canPlace(grid, curio, targetX, targetY, rotated, null)) {
            return false;
        }
        grid.add(curio.copy(), targetX, targetY, rotated);
        source.get().setStackInSlot(index, ItemStack.EMPTY);
        player.getInventory().setChanged();
        return true;
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
