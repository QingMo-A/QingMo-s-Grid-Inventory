package com.dreamingfish.gridinventory.fabric.platform;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.fabric.platform.config.FabricGridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.fabric.platform.config.FabricGridInventoryConfigAccess;
import com.dreamingfish.gridinventory.fabric.platform.menu.FabricGridInventoryMenuBridge;
import com.dreamingfish.gridinventory.fabric.platform.network.FabricGridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.fabric.platform.player.FabricGridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryNetworkBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryPlatform;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import com.dreamingfish.gridinventory.platform.config.GridInventoryClientConfigAccess;
import com.dreamingfish.gridinventory.platform.config.GridInventoryConfigAccess;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FabricGridInventoryPlatform implements GridInventoryPlatform {
    private final GridInventoryRegistryBridge registry = new FabricGridInventoryRegistryBridge();
    private final GridInventoryNetworkBridge network = new FabricGridInventoryNetworkBridge();
    private final GridInventoryPlayerDataBridge playerData = new FabricGridInventoryPlayerDataBridge();
    private final GridInventoryAccessoryBridge accessories = new NoopAccessoryBridge();
    private final GridInventoryMenuBridge menus = new FabricGridInventoryMenuBridge();
    private final GridInventoryConfigAccess config = new FabricGridInventoryConfigAccess();
    private final GridInventoryClientConfigAccess clientConfig = new FabricGridInventoryClientConfigAccess();

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
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
    public GridInventoryRegistryBridge registry() {
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

    // TODO: Replace with Trinkets or another Fabric accessory implementation later.
    private static final class NoopAccessoryBridge implements GridInventoryAccessoryBridge {
        @Override
        public boolean isLoaded() {
            return false;
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
        public List<CuriosSlotView> collectSlots(Player player) {
            return List.of();
        }

        @Override
        public boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
            return false;
        }

        @Override
        public boolean movePlayerSlotToCurio(Player player, int sourcePlayerSlot, String identifier, int index) {
            return false;
        }

        @Override
        public boolean moveGridEntryToCurio(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
            return false;
        }

        @Override
        public boolean moveEquipmentEntryToCurio(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
            return false;
        }

        @Override
        public boolean moveCurioToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
            return false;
        }

        @Override
        public boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
            return false;
        }

        @Override
        public boolean moveCurioToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
            return false;
        }

        @Override
        public Optional<ItemStack> getCurioStack(Player player, String identifier, int index) {
            return Optional.empty();
        }

        @Override
        public void setCurioStack(Player player, String identifier, int index, ItemStack stack) {
        }
    }

}
