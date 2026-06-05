package com.dreamingfish.gridinventory.common.compat.curios;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CuriosIntegration {
    private CuriosIntegration() {
    }

    public static boolean isLoaded() {
        return GridInventoryServices.accessories().isLoaded();
    }

    public static List<CuriosSlotView> collectSlots(Player player) {
        return GridInventoryServices.accessories().collectSlots(player);
    }

    public static boolean movePlayerSlotToCurio(Player player, int sourcePlayerSlot, String identifier, int index) {
        return GridInventoryServices.accessories().movePlayerSlotToCurio(player, sourcePlayerSlot, identifier, index);
    }

    public static boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return GridInventoryServices.accessories().canPlaceInCurio(player, identifier, index, stack, targetEmpty);
    }

    public static boolean moveGridEntryToCurio(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        return GridInventoryServices.accessories().moveGridEntryToCurio(player, grid, entryId, identifier, index);
    }

    public static boolean moveEquipmentEntryToCurio(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        return GridInventoryServices.accessories().moveEquipmentEntryToCurio(player, inventory, entryId, identifier, index);
    }

    public static boolean quickEquip(Player player, ItemStack source) {
        return GridInventoryServices.accessories().quickEquip(player, source);
    }

    public static boolean canQuickEquip(Player player, ItemStack stack) {
        return GridInventoryServices.accessories().canQuickEquip(player, stack);
    }

    public static boolean moveCurioToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        return GridInventoryServices.accessories().moveCurioToPlayerSlot(player, identifier, index, targetPlayerSlot);
    }

    public static boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
        return GridInventoryServices.accessories().moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated);
    }

    public static boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return GridInventoryServices.accessories().moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated, targetFolded);
    }

    public static Optional<ItemStack> getCurioStack(Player player, String identifier, int index) {
        return GridInventoryServices.accessories().getCurioStack(player, identifier, index);
    }

    public static void setCurioStack(Player player, String identifier, int index, ItemStack stack) {
        GridInventoryServices.accessories().setCurioStack(player, identifier, index, stack);
    }
}
