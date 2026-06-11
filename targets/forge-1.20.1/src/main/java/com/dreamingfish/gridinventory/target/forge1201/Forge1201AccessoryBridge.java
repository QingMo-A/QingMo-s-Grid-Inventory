package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.AccessorySlotView;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Forge1201AccessoryBridge implements GridInventoryAccessoryBridge {
    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void registerBackpackAccessories() {
    }

    @Override
    public boolean canQuickEquip(Player player, ItemStack stack) {
        return false;
    }

    @Override
    public boolean quickEquip(Player player, ItemStack source) {
        return false;
    }

    @Override
    public List<AccessorySlotView> collectSlots(Player player) {
        return List.of();
    }

    @Override
    public boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return false;
    }

    @Override
    public boolean movePlayerSlotToAccessory(Player player, int sourcePlayerSlot, String identifier, int index) {
        return false;
    }

    @Override
    public boolean moveGridEntryToAccessory(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        return false;
    }

    @Override
    public boolean moveEquipmentEntryToAccessory(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        return false;
    }

    @Override
    public boolean moveAccessoryToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        return false;
    }

    @Override
    public boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
        return false;
    }

    @Override
    public boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return false;
    }

    @Override
    public Optional<ItemStack> getAccessoryStack(Player player, String identifier, int index) {
        return Optional.empty();
    }

    @Override
    public void setAccessoryStack(Player player, String identifier, int index, ItemStack stack) {
    }
}
