package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;

public final class NeoForgeGridInventoryConfigAccess implements GridInventoryConfigAccess {
    @Override
    public boolean enableGridInventory() {
        return GridInventoryConfig.ENABLE_GRID_INVENTORY.get();
    }

    @Override
    public int defaultItemWidth() {
        return GridInventoryConfig.DEFAULT_ITEM_WIDTH.get();
    }

    @Override
    public int defaultItemHeight() {
        return GridInventoryConfig.DEFAULT_ITEM_HEIGHT.get();
    }

    @Override
    public boolean defaultRotatable() {
        return GridInventoryConfig.DEFAULT_ROTATABLE.get();
    }

    @Override
    public boolean gridItemsStackable() {
        return GridInventoryConfig.GRID_ITEMS_STACKABLE.get();
    }

    @Override
    public int maxNestedBackpackDepth() {
        return GridInventoryConfig.MAX_NESTED_BACKPACK_DEPTH.get();
    }

    @Override
    public int smallGridBagColumns() {
        return GridInventoryConfig.SMALL_GRID_BAG_COLUMNS.get();
    }

    @Override
    public int smallGridBagRows() {
        return GridInventoryConfig.SMALL_GRID_BAG_ROWS.get();
    }

    @Override
    public boolean replaceSurvivalInventory() {
        return GridInventoryConfig.REPLACE_SURVIVAL_INVENTORY.get();
    }

    @Override
    public int playerGridColumns() {
        return GridInventoryConfig.PLAYER_GRID_COLUMNS.get();
    }

    @Override
    public int playerGridRows() {
        return GridInventoryConfig.PLAYER_GRID_ROWS.get();
    }

    @Override
    public boolean pocketEnabled() {
        return GridInventoryConfig.POCKET_ENABLED.get();
    }

    @Override
    public int pocketColumns() {
        return GridInventoryConfig.POCKET_COLUMNS.get();
    }

    @Override
    public int pocketRows() {
        return GridInventoryConfig.POCKET_ROWS.get();
    }

    @Override
    public boolean equipmentStorageEnabled() {
        return GridInventoryConfig.EQUIPMENT_STORAGE_ENABLED.get();
    }

    @Override
    public int foldedBackpackWidth() {
        return GridInventoryConfig.FOLDED_BACKPACK_WIDTH.get();
    }

    @Override
    public int foldedBackpackHeight() {
        return GridInventoryConfig.FOLDED_BACKPACK_HEIGHT.get();
    }

    @Override
    public boolean allowChestStorage() {
        return GridInventoryConfig.ALLOW_CHEST_STORAGE.get();
    }

    @Override
    public boolean allowLegsStorage() {
        return GridInventoryConfig.ALLOW_LEGS_STORAGE.get();
    }

    @Override
    public boolean customHotbarSlotsEnabled() {
        return GridInventoryConfig.CUSTOM_HOTBAR_SLOTS_ENABLED.get();
    }

    @Override
    public int hotbarSlots() {
        return GridInventoryConfig.HOTBAR_SLOTS.get();
    }

    @Override
    public boolean disableVanillaAutoPickup() {
        return GridInventoryConfig.DISABLE_VANILLA_AUTO_PICKUP.get();
    }

    @Override
    public boolean manualPickupEnabled() {
        return GridInventoryConfig.MANUAL_PICKUP_ENABLED.get();
    }

    @Override
    public double pickupRange() {
        return GridInventoryConfig.PICKUP_RANGE.get();
    }

    @Override
    public double nearbyItemsRange() {
        return GridInventoryConfig.NEARBY_ITEMS_RANGE.get();
    }

    @Override
    public boolean allowPickupThroughWalls() {
        return GridInventoryConfig.ALLOW_PICKUP_THROUGH_WALLS.get();
    }

    @Override
    public boolean serverValidateNearbyRange() {
        return GridInventoryConfig.SERVER_VALIDATE_NEARBY_RANGE.get();
    }
}
