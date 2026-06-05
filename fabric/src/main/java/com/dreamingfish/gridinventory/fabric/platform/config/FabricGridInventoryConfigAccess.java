package com.dreamingfish.gridinventory.fabric.platform.config;

import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;

/**
 * TODO: Load Fabric values from config/df_grid_inventory.json or a future config UI.
 */
public final class FabricGridInventoryConfigAccess implements GridInventoryConfigAccess {
    @Override
    public boolean enableGridInventory() {
        return true;
    }

    @Override
    public int defaultItemWidth() {
        return 1;
    }

    @Override
    public int defaultItemHeight() {
        return 1;
    }

    @Override
    public boolean defaultRotatable() {
        return false;
    }

    @Override
    public boolean gridItemsStackable() {
        return true;
    }

    @Override
    public int smallGridBagColumns() {
        return 8;
    }

    @Override
    public int smallGridBagRows() {
        return 6;
    }

    @Override
    public boolean replaceSurvivalInventory() {
        return true;
    }

    @Override
    public int playerGridColumns() {
        return 10;
    }

    @Override
    public int playerGridRows() {
        return 6;
    }

    @Override
    public boolean pocketEnabled() {
        return true;
    }

    @Override
    public int pocketColumns() {
        return 4;
    }

    @Override
    public int pocketRows() {
        return 1;
    }

    @Override
    public boolean equipmentStorageEnabled() {
        return true;
    }

    @Override
    public int foldedBackpackWidth() {
        return 2;
    }

    @Override
    public int foldedBackpackHeight() {
        return 2;
    }

    @Override
    public boolean allowChestStorage() {
        return true;
    }

    @Override
    public boolean allowLegsStorage() {
        return true;
    }

    @Override
    public boolean customHotbarSlotsEnabled() {
        return false;
    }

    @Override
    public int hotbarSlots() {
        return 9;
    }

    @Override
    public boolean disableVanillaAutoPickup() {
        return true;
    }

    @Override
    public boolean manualPickupEnabled() {
        return true;
    }

    @Override
    public double pickupRange() {
        return 2.0D;
    }

    @Override
    public double nearbyItemsRange() {
        return 1.0D;
    }

    @Override
    public boolean allowPickupThroughWalls() {
        return false;
    }

    @Override
    public boolean serverValidateNearbyRange() {
        return true;
    }
}
