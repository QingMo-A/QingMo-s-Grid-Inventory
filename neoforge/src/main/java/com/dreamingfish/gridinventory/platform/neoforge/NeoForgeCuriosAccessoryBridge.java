package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.neoforge.compat.NeoForgeCuriosIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NeoForgeCuriosAccessoryBridge implements GridInventoryAccessoryBridge {
    @Override
    public boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    @Override
    public boolean canQuickEquip(Player player, ItemStack stack) {
        return NeoForgeCuriosIntegration.canQuickEquip(player, stack);
    }

    @Override
    public boolean quickEquip(Player player, ItemStack source) {
        return NeoForgeCuriosIntegration.quickEquip(player, source);
    }

    @Override
    public List<CuriosSlotView> collectSlots(Player player) {
        return NeoForgeCuriosIntegration.collectSlots(player);
    }

    @Override
    public boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return NeoForgeCuriosIntegration.canPlaceInCurio(player, identifier, index, stack, targetEmpty);
    }

    @Override
    public boolean movePlayerSlotToCurio(Player player, int sourcePlayerSlot, String identifier, int index) {
        return NeoForgeCuriosIntegration.movePlayerSlotToCurio(player, sourcePlayerSlot, identifier, index);
    }

    @Override
    public boolean moveGridEntryToCurio(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        return NeoForgeCuriosIntegration.moveGridEntryToCurio(player, grid, entryId, identifier, index);
    }

    @Override
    public boolean moveEquipmentEntryToCurio(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        return NeoForgeCuriosIntegration.moveEquipmentEntryToCurio(player, inventory, entryId, identifier, index);
    }

    @Override
    public boolean moveCurioToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        return NeoForgeCuriosIntegration.moveCurioToPlayerSlot(player, identifier, index, targetPlayerSlot);
    }

    @Override
    public boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
        return NeoForgeCuriosIntegration.moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated);
    }

    @Override
    public boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return NeoForgeCuriosIntegration.moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated, targetFolded);
    }

    @Override
    public Optional<ItemStack> getCurioStack(Player player, String identifier, int index) {
        return NeoForgeCuriosIntegration.getCurioStack(player, identifier, index);
    }

    @Override
    public void setCurioStack(Player player, String identifier, int index, ItemStack stack) {
        NeoForgeCuriosIntegration.setCurioStack(player, identifier, index, stack);
    }
}
