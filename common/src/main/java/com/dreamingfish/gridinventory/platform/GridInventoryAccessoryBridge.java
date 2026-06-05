package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GridInventoryAccessoryBridge {
    boolean isLoaded();

    boolean canQuickEquip(Player player, ItemStack stack);

    boolean quickEquip(Player player, ItemStack source);

    List<CuriosSlotView> collectSlots(Player player);

    boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty);

    boolean movePlayerSlotToCurio(Player player, int sourcePlayerSlot, String identifier, int index);

    boolean moveGridEntryToCurio(Player player, GridInventoryData grid, UUID entryId, String identifier, int index);

    boolean moveEquipmentEntryToCurio(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index);

    boolean moveCurioToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot);

    boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated);

    boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded);

    Optional<ItemStack> getCurioStack(Player player, String identifier, int index);

    void setCurioStack(Player player, String identifier, int index, ItemStack stack);
}
