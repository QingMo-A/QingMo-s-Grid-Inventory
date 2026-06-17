package com.dreamingfish.gridinventory.target.forge1201.config;

import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;

public final class Forge1201ServerConfigAccess implements GridInventoryConfigAccess {
    @Override public boolean enableGridInventory() { return Forge1201ServerConfig.ENABLE_GRID_INVENTORY.get(); }
    @Override public int defaultItemWidth() { return Forge1201ServerConfig.DEFAULT_ITEM_WIDTH.get(); }
    @Override public int defaultItemHeight() { return Forge1201ServerConfig.DEFAULT_ITEM_HEIGHT.get(); }
    @Override public boolean defaultRotatable() { return Forge1201ServerConfig.DEFAULT_ROTATABLE.get(); }
    @Override public boolean gridItemsStackable() { return Forge1201ServerConfig.GRID_ITEMS_STACKABLE.get(); }
    @Override public int maxNestedBackpackDepth() { return Forge1201ServerConfig.MAX_NESTED_BACKPACK_DEPTH.get(); }
    @Override public int smallGridBagColumns() { return Forge1201ServerConfig.SMALL_GRID_BAG_COLUMNS.get(); }
    @Override public int smallGridBagRows() { return Forge1201ServerConfig.SMALL_GRID_BAG_ROWS.get(); }
    @Override public boolean replaceSurvivalInventory() { return Forge1201ServerConfig.REPLACE_SURVIVAL_INVENTORY.get(); }
    @Override public int playerGridColumns() { return Forge1201ServerConfig.PLAYER_GRID_COLUMNS.get(); }
    @Override public int playerGridRows() { return Forge1201ServerConfig.PLAYER_GRID_ROWS.get(); }
    @Override public boolean pocketEnabled() { return Forge1201ServerConfig.POCKET_ENABLED.get(); }
    @Override public int pocketColumns() { return Forge1201ServerConfig.POCKET_COLUMNS.get(); }
    @Override public int pocketRows() { return Forge1201ServerConfig.POCKET_ROWS.get(); }
    @Override public boolean equipmentStorageEnabled() { return Forge1201ServerConfig.EQUIPMENT_STORAGE_ENABLED.get(); }
    @Override public int foldedBackpackWidth() { return Forge1201ServerConfig.FOLDED_BACKPACK_WIDTH.get(); }
    @Override public int foldedBackpackHeight() { return Forge1201ServerConfig.FOLDED_BACKPACK_HEIGHT.get(); }
    @Override public boolean allowChestStorage() { return Forge1201ServerConfig.ALLOW_CHEST_STORAGE.get(); }
    @Override public boolean allowLegsStorage() { return Forge1201ServerConfig.ALLOW_LEGS_STORAGE.get(); }
    @Override public boolean customHotbarSlotsEnabled() { return Forge1201ServerConfig.CUSTOM_HOTBAR_SLOTS_ENABLED.get(); }
    @Override public int hotbarSlots() { return Forge1201ServerConfig.HOTBAR_SLOTS.get(); }
    @Override public boolean disableVanillaAutoPickup() { return Forge1201ServerConfig.DISABLE_VANILLA_AUTO_PICKUP.get(); }
    @Override public boolean manualPickupEnabled() { return Forge1201ServerConfig.MANUAL_PICKUP_ENABLED.get(); }
    @Override public double pickupRange() { return Forge1201ServerConfig.PICKUP_RANGE.get(); }
    @Override public double nearbyItemsRange() { return Forge1201ServerConfig.NEARBY_ITEMS_RANGE.get(); }
    @Override public boolean allowPickupThroughWalls() { return Forge1201ServerConfig.ALLOW_PICKUP_THROUGH_WALLS.get(); }
    @Override public boolean serverValidateNearbyRange() { return Forge1201ServerConfig.SERVER_VALIDATE_NEARBY_RANGE.get(); }
}
