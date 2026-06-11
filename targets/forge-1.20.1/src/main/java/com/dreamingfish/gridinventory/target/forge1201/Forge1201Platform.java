package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryItemStackDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryPlatform;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import com.dreamingfish.gridinventory.target.forge1201.platform.Forge1201RegistryBridge;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class Forge1201Platform implements GridInventoryPlatform {
    private final GridInventoryItemStackDataBridge itemStackData = new Forge1201ItemStackDataBridge();
    private final GridInventoryPlayerDataBridge playerData = new Forge1201PlayerDataBridge();
    private final GridInventoryAccessoryBridge accessories = new Forge1201AccessoryBridge();
    private final Forge1201RegistryBridge registry = new Forge1201RegistryBridge();
    private final GridInventoryMenuBridge menus = new Forge1201MenuBridge();
    private final GridInventoryConfigAccess config = new Forge1201ConfigAccess();

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, Player player) {
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot() == slot;
        }
        return slot == EquipmentSlot.CHEST && stack.getItem() instanceof ElytraItem;
    }

    @Override
    public GridInventoryNetworkBridge network() {
        throw unsupported("NetworkBridge");
    }

    @Override
    public GridInventoryPlayerDataBridge playerData() {
        return playerData;
    }

    @Override
    public GridInventoryAccessoryBridge accessories() {
        return accessories;
    }

    @Override
    public GridInventoryItemStackDataBridge itemStackData() {
        return itemStackData;
    }

    @Override
    public Forge1201RegistryBridge registry() {
        return registry;
    }

    @Override
    public GridInventoryMenuBridge menus() {
        return menus;
    }

    @Override
    public GridInventoryConfigAccess config() {
        return config;
    }

    @Override
    public GridInventoryClientConfigAccess clientConfig() {
        throw unsupported("ClientConfigAccess");
    }

    private static UnsupportedOperationException unsupported(String bridge) {
        return new UnsupportedOperationException("Forge 1.20.1 " + bridge + " is not implemented yet");
    }

    private static final class Forge1201ConfigAccess implements GridInventoryConfigAccess {
        @Override public boolean enableGridInventory() { return true; }
        @Override public int defaultItemWidth() { return 1; }
        @Override public int defaultItemHeight() { return 1; }
        @Override public boolean defaultRotatable() { return true; }
        @Override public boolean gridItemsStackable() { return true; }
        @Override public int smallGridBagColumns() { return 5; }
        @Override public int smallGridBagRows() { return 4; }
        @Override public boolean replaceSurvivalInventory() { return true; }
        @Override public int playerGridColumns() { return 9; }
        @Override public int playerGridRows() { return 4; }
        @Override public boolean pocketEnabled() { return true; }
        @Override public int pocketColumns() { return 9; }
        @Override public int pocketRows() { return 4; }
        @Override public boolean equipmentStorageEnabled() { return true; }
        @Override public int foldedBackpackWidth() { return 2; }
        @Override public int foldedBackpackHeight() { return 2; }
        @Override public boolean allowChestStorage() { return true; }
        @Override public boolean allowLegsStorage() { return true; }
        @Override public boolean customHotbarSlotsEnabled() { return false; }
        @Override public int hotbarSlots() { return 9; }
        @Override public boolean disableVanillaAutoPickup() { return false; }
        @Override public boolean manualPickupEnabled() { return true; }
        @Override public double pickupRange() { return 4.5D; }
        @Override public double nearbyItemsRange() { return 6.0D; }
        @Override public boolean allowPickupThroughWalls() { return false; }
        @Override public boolean serverValidateNearbyRange() { return true; }
    }
}
