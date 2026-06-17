package com.dreamingfish.gridinventory.platform.config;

public interface GridInventoryConfigAccess {
    boolean enableGridInventory();

    int defaultItemWidth();

    int defaultItemHeight();

    boolean defaultRotatable();

    boolean gridItemsStackable();

    int maxNestedBackpackDepth();

    int smallGridBagColumns();

    int smallGridBagRows();

    boolean replaceSurvivalInventory();

    int playerGridColumns();

    int playerGridRows();

    boolean pocketEnabled();

    int pocketColumns();

    int pocketRows();

    boolean equipmentStorageEnabled();

    int foldedBackpackWidth();

    int foldedBackpackHeight();

    boolean allowChestStorage();

    boolean allowLegsStorage();

    boolean customHotbarSlotsEnabled();

    int hotbarSlots();

    boolean disableVanillaAutoPickup();

    boolean manualPickupEnabled();

    double pickupRange();

    double nearbyItemsRange();

    boolean allowPickupThroughWalls();

    boolean serverValidateNearbyRange();
}
