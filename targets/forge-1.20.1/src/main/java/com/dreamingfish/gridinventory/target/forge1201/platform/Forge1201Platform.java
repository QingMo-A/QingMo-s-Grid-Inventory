package com.dreamingfish.gridinventory.target.forge1201.platform;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.platform.AccessorySlotView;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryItemStackDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryPlatform;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201ProtocolCompat;
import com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201SimpleChannelBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Forge1201Platform implements GridInventoryPlatform {
    private final GridInventoryNetworkBridge network = new Forge1201NetworkBridge();
    private final GridInventoryPlayerDataBridge playerData = new Forge1201PlayerDataBridge();
    private final GridInventoryAccessoryBridge accessories = new Forge1201AccessoryBridge();
    private final GridInventoryItemStackDataBridge itemStackData = new Forge1201ItemStackDataBridge();
    private final Forge1201RegistryBridge registry = new Forge1201RegistryBridge();
    private final GridInventoryMenuBridge menus = new Forge1201MenuBridge();
    private final GridInventoryConfigAccess config = new Forge1201ConfigAccess();
    private final GridInventoryClientConfigAccess clientConfig = new Forge1201ClientConfigAccess();

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public GridInventoryNetworkBridge network() {
        return network;
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
        return clientConfig;
    }

    private static final class Forge1201NetworkBridge implements GridInventoryNetworkBridge {
        @Override
        public void registerMessages() {
            Forge1201ProtocolCompat.register();
        }

        @Override
        public void sendToServer(GridMessage message) {
            Forge1201SimpleChannelBridge.sendToServer(message);
        }

        @Override
        public void sendToPlayer(ServerPlayer player, GridMessage message) {
            throw unsupported("sendToPlayer");
        }

        @Override
        public void sendToAllPlayers(GridMessage message) {
            throw unsupported("sendToAllPlayers");
        }

        @Override
        public void syncMenu(Player player, GridInventoryMenu menu) {
            throw unsupported("syncMenu");
        }

        @Override
        public void syncEquipmentStorage(Player player, EquipmentSlot slot, EquipmentStorageData storage) {
            throw unsupported("syncEquipmentStorage");
        }
    }

    private static final class Forge1201PlayerDataBridge implements GridInventoryPlayerDataBridge {
        @Override
        public GridInventoryData getPlayerGridInventory(Player player) {
            throw unsupported("getPlayerGridInventory");
        }

        @Override
        public void setPlayerGridInventory(Player player, GridInventoryData data) {
            throw unsupported("setPlayerGridInventory");
        }

        @Override
        public GridInventoryData copyPlayerGridInventory(Player player) {
            throw unsupported("copyPlayerGridInventory");
        }
    }

    private static final class Forge1201AccessoryBridge implements GridInventoryAccessoryBridge {
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

    private static final class Forge1201MenuBridge implements GridInventoryMenuBridge {
        @Override
        public void openGridInventory(ServerPlayer player, int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data) {
            throw unsupported("openGridInventory");
        }
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

    private static final class Forge1201ClientConfigAccess implements GridInventoryClientConfigAccess {
        @Override public String pickupKeyDefault() { return "key.keyboard.r"; }
        @Override public boolean highlightTargetItem() { return true; }
        @Override public String highlightColor() { return "#FFFFFF"; }
        @Override public boolean showNearbyItemsPanel() { return true; }
        @Override public boolean enableSurvivalInventoryGridUi() { return true; }
        @Override public boolean showPlayerModel() { return true; }
        @Override public boolean showEquipmentPanel() { return true; }
        @Override public boolean showGridColumn() { return true; }
        @Override public String layoutMode() { return "default"; }
        @Override public int columnGap() { return 8; }
        @Override public boolean enableGridColumnScroll() { return true; }
        @Override public boolean enableNearbyColumnScroll() { return true; }
        @Override public int gridCellSize() { return 27; }
        @Override public int gridItemInnerPadding() { return 2; }
        @Override public int equipmentFreeSlotSize() { return 18; }
        @Override public int hotbarFreeSlotSize() { return 18; }
        @Override public int freeSlotItemPadding() { return 1; }
        @Override public int nearbyPanelColumns() { return 4; }
        @Override public int nearbyPanelVisibleRows() { return 6; }
    }

    private static UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException("Forge 1.20.1 platform stub has not implemented " + operation);
    }
}
