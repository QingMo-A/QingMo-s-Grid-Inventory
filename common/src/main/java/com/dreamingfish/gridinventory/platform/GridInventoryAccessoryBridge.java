package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GridInventoryAccessoryBridge {
    boolean isLoaded();

    void registerBackpackAccessories();

    boolean canQuickEquip(Player player, ItemStack stack);

    boolean quickEquip(Player player, ItemStack source);

    List<AccessorySlotView> collectSlots(Player player);

    boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty);

    boolean movePlayerSlotToAccessory(Player player, int sourcePlayerSlot, String identifier, int index);

    boolean moveGridEntryToAccessory(Player player, GridInventoryData grid, UUID entryId, String identifier, int index);

    boolean moveEquipmentEntryToAccessory(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index);

    boolean insertStackIntoAccessory(Player player, ItemStack source, String identifier, int index);

    boolean moveAccessoryToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot);

    boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated);

    boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded);

    Optional<ItemStack> getAccessoryStack(Player player, String identifier, int index);

    void setAccessoryStack(Player player, String identifier, int index, ItemStack stack);
}
