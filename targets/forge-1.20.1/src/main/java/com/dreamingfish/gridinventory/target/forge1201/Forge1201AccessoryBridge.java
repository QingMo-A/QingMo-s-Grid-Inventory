package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.AccessorySlotView;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import net.minecraftforge.fml.ModList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Forge1201AccessoryBridge implements GridInventoryAccessoryBridge {
    private static final String IMPLEMENTATION =
            "com.dreamingfish.gridinventory.target.forge1201.compat.curios.Forge1201CuriosAccessoryBridge";
    private static final GridInventoryAccessoryBridge NOOP = new NoOpAccessoryBridge();

    private volatile GridInventoryAccessoryBridge delegate;

    private GridInventoryAccessoryBridge delegate() {
        GridInventoryAccessoryBridge current = delegate;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (delegate == null) {
                delegate = loadDelegate();
            }
            return delegate;
        }
    }

    private GridInventoryAccessoryBridge loadDelegate() {
        if (!ModList.get().isLoaded("curios")) {
            return NOOP;
        }
        try {
            Class<?> type = Class.forName(IMPLEMENTATION, true, Forge1201AccessoryBridge.class.getClassLoader());
            Object instance = type.getDeclaredConstructor().newInstance();
            if (instance instanceof GridInventoryAccessoryBridge bridge) {
                return bridge;
            }
            DFGridInventory.LOGGER.error("Forge Curios bridge does not implement GridInventoryAccessoryBridge");
        } catch (ReflectiveOperationException | LinkageError exception) {
            DFGridInventory.LOGGER.error("Failed to initialize optional Forge Curios integration", exception);
        }
        return NOOP;
    }

    @Override
    public boolean isLoaded() {
        return delegate() != NOOP;
    }

    @Override
    public void registerBackpackAccessories() {
        delegate().registerBackpackAccessories();
    }

    @Override
    public boolean canQuickEquip(Player player, ItemStack stack) {
        return delegate().canQuickEquip(player, stack);
    }

    @Override
    public boolean quickEquip(Player player, ItemStack source) {
        return delegate().quickEquip(player, source);
    }

    @Override
    public List<AccessorySlotView> collectSlots(Player player) {
        return delegate().collectSlots(player);
    }

    @Override
    public boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return delegate().canPlaceInCurio(player, identifier, index, stack, targetEmpty);
    }

    @Override
    public boolean movePlayerSlotToAccessory(Player player, int sourcePlayerSlot, String identifier, int index) {
        return delegate().movePlayerSlotToAccessory(player, sourcePlayerSlot, identifier, index);
    }

    @Override
    public boolean moveGridEntryToAccessory(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        return delegate().moveGridEntryToAccessory(player, grid, entryId, identifier, index);
    }

    @Override
    public boolean moveEquipmentEntryToAccessory(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        return delegate().moveEquipmentEntryToAccessory(player, inventory, entryId, identifier, index);
    }

    @Override
    public boolean moveAccessoryToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        return delegate().moveAccessoryToPlayerSlot(player, identifier, index, targetPlayerSlot);
    }

    @Override
    public boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
        return delegate().moveAccessoryToGrid(player, grid, identifier, index, targetX, targetY, rotated);
    }

    @Override
    public boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return delegate().moveAccessoryToGrid(player, grid, identifier, index, targetX, targetY, rotated, targetFolded);
    }

    @Override
    public Optional<ItemStack> getAccessoryStack(Player player, String identifier, int index) {
        return delegate().getAccessoryStack(player, identifier, index);
    }

    @Override
    public void setAccessoryStack(Player player, String identifier, int index, ItemStack stack) {
        delegate().setAccessoryStack(player, identifier, index, stack);
    }

    private static final class NoOpAccessoryBridge implements GridInventoryAccessoryBridge {
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
}
