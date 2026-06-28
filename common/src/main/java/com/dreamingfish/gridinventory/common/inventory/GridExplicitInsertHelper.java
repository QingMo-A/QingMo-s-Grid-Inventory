package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class GridExplicitInsertHelper {
    private GridExplicitInsertHelper() {
    }

    public static int insertOrMergeAt(GridInventoryData inventory, ItemStack incoming, int targetX, int targetY,
                                      boolean rotated, int targetDepth) {
        return insertOrMergeAt(inventory, incoming, targetX, targetY, rotated, null, targetDepth);
    }

    public static int insertOrMergeAt(GridInventoryData inventory, ItemStack incoming, int targetX, int targetY,
                                      boolean rotated, @Nullable UUID ignoredEntryId, int targetDepth) {
        if (incoming.isEmpty()) {
            return 0;
        }
        var targetEntry = inventory.getEntries().stream()
                .filter(entry -> ignoredEntryId == null || !ignoredEntryId.equals(entry.entryId()))
                .filter(entry -> entry.contains(targetX, targetY))
                .findFirst();
        if (targetEntry.isPresent()) {
            if (!GridStackMerger.itemsStackableInGrid()) {
                return 0;
            }
            ItemStack existing = targetEntry.get().stack();
            if (!GridItemStacks.sameItemSameData(existing, incoming)
                    || existing.getCount() >= existing.getMaxStackSize()) {
                return 0;
            }
            int moved = Math.min(incoming.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (moved <= 0) {
                return 0;
            }
            existing.grow(moved);
            inventory.setChanged();
            return moved;
        }
        if (!GridPlacementValidator.canPlace(inventory, incoming, targetX, targetY, rotated, ignoredEntryId,
                targetDepth)) {
            return 0;
        }
        inventory.add(incoming.copy(), targetX, targetY, rotated);
        return incoming.getCount();
    }
}
