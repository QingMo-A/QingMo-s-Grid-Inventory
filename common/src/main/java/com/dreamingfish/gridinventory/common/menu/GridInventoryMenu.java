package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.GridStackMerger;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.item.SmallGridBagItem;
import com.dreamingfish.gridinventory.common.network.ModNetworking;
import com.dreamingfish.gridinventory.common.pickup.ManualPickupHandler;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.Optional;

public class GridInventoryMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final int bagSlot;
    private final InteractionHand hand;
    private final boolean playerGrid;
    private GridInventoryData gridData;

    public GridInventoryMenu(int containerId, Inventory playerInventory, int bagSlot, InteractionHand hand, GridInventoryData gridData) {
        this(containerId, playerInventory, bagSlot, hand, gridData, false);
    }

    public GridInventoryMenu(int containerId, Inventory playerInventory, int bagSlot, InteractionHand hand, GridInventoryData gridData, boolean playerGrid) {
        super(ModMenus.GRID_INVENTORY.get(), containerId);
        this.playerInventory = playerInventory;
        this.bagSlot = bagSlot;
        this.hand = hand;
        this.playerGrid = playerGrid;
        this.gridData = gridData;
        this.gridData.setChangeListener(this::save);
        addPlayerSlots(playerInventory);
    }

    public static GridInventoryMenu fromOpenData(int containerId, Inventory playerInventory, GridInventoryMenuOpenData data) {
        return new GridInventoryMenu(containerId, playerInventory, data.sourceSlot(), data.hand(), data.data(), data.playerInventory());
    }

    private void addPlayerSlots(Inventory inventory) {
        if (playerGrid) {
            this.addSlot(new EquipmentSlotView(inventory, 39, 12, 64, EquipmentSlot.HEAD));
            this.addSlot(new EquipmentSlotView(inventory, 38, 12, 84, EquipmentSlot.CHEST));
            this.addSlot(new EquipmentSlotView(inventory, 37, 12, 104, EquipmentSlot.LEGS));
            this.addSlot(new EquipmentSlotView(inventory, 36, 12, 124, EquipmentSlot.FEET));
            this.addSlot(new Slot(inventory, 40, 12, 150));
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column, 86 + column * 18, 198));
            }
            return;
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        }
    }

    public GridInventoryData getGridData() {
        return gridData;
    }

    public boolean isPlayerGrid() {
        return playerGrid;
    }

    public void replaceGridData(GridInventoryData data) {
        this.gridData = data;
        this.gridData.setChangeListener(this::save);
    }

    public boolean insertFromPlayerInventory(int playerSlot, int targetX, int targetY, boolean rotated) {
        return insertFromPlayerInventory(playerSlot, targetX, targetY, rotated, false);
    }

    public boolean insertFromPlayerInventory(int playerSlot, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        if (source.isEmpty()) {
            return false;
        }
        ItemStack placedStack = source.copy();
        if (placedStack.getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(placedStack)) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(placedStack, targetFolded);
        }
        var targetEntry = gridData.getEntries().stream().filter(entry -> entry.contains(targetX, targetY)).findFirst();
        if (GridStackMerger.itemsStackableInGrid()
                && targetEntry.isPresent()
                && GridItemStacks.sameItemSameData(targetEntry.get().stack(), placedStack)
                && targetEntry.get().stack().getCount() < targetEntry.get().stack().getMaxStackSize()) {
            int moved = Math.min(source.getCount(), targetEntry.get().stack().getMaxStackSize() - targetEntry.get().stack().getCount());
            targetEntry.get().stack().grow(moved);
            source.shrink(moved);
            gridData.setChanged();
            playerInventory.setChanged();
            save();
            return true;
        }
        if (!GridPlacementValidator.canPlace(gridData, placedStack, targetX, targetY, rotated, null)) {
            return false;
        }
        ItemStack inserted = placedStack.copyWithCount(GridStackMerger.itemsStackableInGrid() ? source.getCount() : 1);
        gridData.add(inserted, targetX, targetY, rotated);
        source.shrink(inserted.getCount());
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean quickInsertFromPlayerInventory(int playerSlot) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        ItemStack remainder = gridData.insert(source.copy(), GridInsertMode.EXECUTE);
        int inserted = source.getCount() - remainder.getCount();
        if (inserted > 0) {
            source.shrink(inserted);
            playerInventory.setChanged();
            save();
            return true;
        }
        return false;
    }

    public boolean moveEntry(UUID entryId, int targetX, int targetY, boolean rotated) {
        return moveEntry(entryId, targetX, targetY, rotated, false);
    }

    public boolean moveEntry(UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        Boolean previousFolded = com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().isBackpackFolded(entry.get().stack());
        if (entry.get().stack().getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(entry.get().stack())) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(), targetFolded);
        }
        boolean moved = gridData.move(entryId, targetX, targetY, rotated);
        if (moved) {
            save();
        } else if (entry.get().stack().getItem() instanceof GridBackpackItem) {
            if (previousFolded == null) {
                com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().removeBackpackFolded(entry.get().stack());
            } else {
                com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(), previousFolded);
            }
        }
        return moved;
    }

    public boolean toggleGridEntryBackpackFold(UUID entryId, int targetX, int targetY, boolean rotated) {
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = gridData.getEntry(entryId);
        if (entry.isEmpty() || !(entry.get().stack().getItem() instanceof GridBackpackItem)) {
            return false;
        }
        ItemStack toggled = entry.get().stack().copy();
        if (!GridBackpackItem.toggleFolded(toggled)) {
            return false;
        }
        if (!GridPlacementValidator.canPlace(gridData, toggled, targetX, targetY, rotated, entryId)) {
            return false;
        }
        com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(), GridBackpackItem.isFolded(toggled));
        boolean moved = gridData.move(entryId, targetX, targetY, rotated);
        if (moved) {
            save();
        }
        return moved;
    }

    public boolean quickEquipGridEntry(UUID entryId) {
        if (!playerGrid) {
            return false;
        }
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        Optional<EquipmentSlot> targetSlot = findEmptyArmorSlot(entry.get().stack());
        if (targetSlot.isPresent()) {
            ItemStack equipped = gridData.extract(entryId, 1);
            playerInventory.setItem(equipmentPlayerSlot(targetSlot.get()), equipped);
            playerInventory.setChanged();
            save();
            return true;
        }
        if (CuriosIntegration.canQuickEquip(playerInventory.player, entry.get().stack())) {
            ItemStack equipped = gridData.extract(entryId, 1);
            boolean equippedToCurio = CuriosIntegration.quickEquip(playerInventory.player, equipped);
            if (equippedToCurio) {
                save();
                return true;
            }
        }
        return false;
    }

    public boolean dropGridEntry(UUID entryId) {
        var entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack dropped = gridData.extract(entryId, entry.get().stack().getCount());
        if (dropped.isEmpty()) {
            return false;
        }
        playerInventory.player.drop(dropped, false);
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean insertFromPlayerIntoEquipmentStorage(int playerSlot, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated) {
        return insertFromPlayerIntoEquipmentStorage(playerSlot, equipmentSlot, containerId, targetX, targetY, rotated, false);
    }

    public boolean insertFromPlayerIntoEquipmentStorage(int playerSlot, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize() || !isFreePlayerSlot(playerSlot)) {
            return false;
        }
        if (equipmentPlayerSlotOrInvalid(equipmentSlot) == playerSlot) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(equipmentSlot, containerId);
        if (source.isEmpty() || edit.isEmpty()) {
            return false;
        }
        ItemStack placedStack = source.copy();
        if (placedStack.getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(placedStack)) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(placedStack, targetFolded);
        }
        if (!GridPlacementValidator.canPlace(edit.get().inventory(), placedStack, targetX, targetY, rotated, null)) {
            return false;
        }
        ItemStack inserted = placedStack.copyWithCount(GridStackMerger.itemsStackableInGrid() ? source.getCount() : 1);
        edit.get().inventory().add(inserted, targetX, targetY, rotated);
        source.shrink(inserted.getCount());
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        playerInventory.setChanged();
        return true;
    }

    public boolean moveEquipmentEntry(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated) {
        return moveEquipmentEntry(equipmentSlot, containerId, entryId, targetX, targetY, rotated, false);
    }

    public boolean moveEquipmentEntry(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(equipmentSlot, containerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = edit.flatMap(value -> value.inventory().getEntry(entryId));
        if (edit.isEmpty() || entry.isEmpty()) {
            return false;
        }
        Boolean previousFolded = com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().isBackpackFolded(entry.get().stack());
        if (entry.get().stack().getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(entry.get().stack())) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(), targetFolded);
        }
        if (!edit.get().inventory().move(entryId, targetX, targetY, rotated)) {
            if (entry.get().stack().getItem() instanceof GridBackpackItem) {
                if (previousFolded == null) {
                    com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().removeBackpackFolded(entry.get().stack());
                } else {
                    com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(), previousFolded);
                }
            }
            return false;
        }
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        return true;
    }

    public boolean toggleEquipmentStorageEntryBackpackFold(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated) {
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(equipmentSlot, containerId);
        if (edit.isEmpty()) {
            return false;
        }
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = edit.get().inventory().getEntry(entryId);
        if (entry.isEmpty() || !(entry.get().stack().getItem() instanceof GridBackpackItem)) {
            return false;
        }
        ItemStack toggled = entry.get().stack().copy();
        if (!GridBackpackItem.toggleFolded(toggled)) {
            return false;
        }
        if (!GridPlacementValidator.canPlace(edit.get().inventory(), toggled, targetX, targetY, rotated, entryId)) {
            return false;
        }
        com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(entry.get().stack(), GridBackpackItem.isFolded(toggled));
        if (!edit.get().inventory().move(entryId, targetX, targetY, rotated)) {
            return false;
        }
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        return true;
    }

    public boolean quickEquipEquipmentStorageEntry(EquipmentSlot sourceSlot, String containerId, UUID entryId) {
        if (!playerGrid) {
            return false;
        }
        Optional<EquipmentStorageEdit> source = editableEquipmentInventory(sourceSlot, containerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = source.flatMap(edit -> edit.inventory().getEntry(entryId));
        if (source.isEmpty() || entry.isEmpty()) {
            return false;
        }
        Optional<EquipmentSlot> targetSlot = findEmptyArmorSlot(entry.get().stack());
        if (targetSlot.isPresent()) {
            ItemStack equipped = source.get().inventory().extract(entryId, 1);
            saveEquipmentStorage(sourceSlot, source.get().storage());
            playerInventory.setItem(equipmentPlayerSlot(targetSlot.get()), equipped);
            playerInventory.setChanged();
            return true;
        }
        if (CuriosIntegration.canQuickEquip(playerInventory.player, entry.get().stack())) {
            ItemStack equipped = source.get().inventory().extract(entryId, 1);
            boolean equippedToCurio = CuriosIntegration.quickEquip(playerInventory.player, equipped);
            if (equippedToCurio) {
                saveEquipmentStorage(sourceSlot, source.get().storage());
                return true;
            }
        }
        return false;
    }

    public boolean dropEquipmentStorageEntry(EquipmentSlot equipmentSlot, String containerId, UUID entryId) {
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(equipmentSlot, containerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = edit.flatMap(storage -> storage.inventory().getEntry(entryId));
        if (edit.isEmpty() || entry.isEmpty()) {
            return false;
        }
        ItemStack dropped = edit.get().inventory().extract(entryId, entry.get().stack().getCount());
        if (dropped.isEmpty()) {
            return false;
        }
        playerInventory.player.drop(dropped, false);
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        playerInventory.setChanged();
        return true;
    }

    public boolean pickupGroundItemIntoGrid(int entityId, int targetX, int targetY, boolean rotated) {
        if (!(playerInventory.player instanceof ServerPlayer player)) {
            return false;
        }
        Optional<ItemEntity> itemEntity = ManualPickupHandler.findReachableItem(player, entityId);
        if (itemEntity.isEmpty() || !insertGroundStackIntoGrid(itemEntity.get(), gridData, targetX, targetY, rotated)) {
            return false;
        }
        save();
        ModNetworking.syncMenu(player, this);
        return true;
    }

    public boolean pickupGroundItemIntoEquipmentStorage(int entityId, EquipmentSlot equipmentSlot, String containerId,
                                                       int targetX, int targetY, boolean rotated) {
        if (!(playerInventory.player instanceof ServerPlayer player)) {
            return false;
        }
        Optional<ItemEntity> itemEntity = ManualPickupHandler.findReachableItem(player, entityId);
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(equipmentSlot, containerId);
        if (itemEntity.isEmpty() || edit.isEmpty()
                || !insertGroundStackIntoGrid(itemEntity.get(), edit.get().inventory(), targetX, targetY, rotated)) {
            return false;
        }
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        return true;
    }

    private boolean insertGroundStackIntoGrid(ItemEntity itemEntity, GridInventoryData inventory, int targetX, int targetY, boolean rotated) {
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty() || !GridPlacementValidator.canPlace(inventory, groundStack, targetX, targetY, rotated, null)) {
            return false;
        }
        ItemStack inserted = groundStack.copyWithCount(GridStackMerger.itemsStackableInGrid() ? groundStack.getCount() : 1);
        inventory.add(inserted, targetX, targetY, rotated);
        groundStack.shrink(inserted.getCount());
        if (groundStack.isEmpty()) {
            itemEntity.discard();
        }
        playerInventory.player.level().playSound(null, playerInventory.player.getX(), playerInventory.player.getY(), playerInventory.player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                ((playerInventory.player.getRandom().nextFloat() - playerInventory.player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        return true;
    }

    public boolean transferGridEntryIntoEquipmentStorage(UUID entryId, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated) {
        return transferGridEntryIntoEquipmentStorage(entryId, equipmentSlot, containerId, targetX, targetY, rotated, false);
    }

    public boolean transferGridEntryIntoEquipmentStorage(UUID entryId, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> source = gridData.getEntry(entryId);
        Optional<EquipmentStorageEdit> target = editableEquipmentInventory(equipmentSlot, containerId);
        if (!playerGrid || source.isEmpty() || target.isEmpty()) {
            return false;
        }
        ItemStack moved = source.get().stack().copy();
        if (moved.getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(moved)) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(moved, targetFolded);
        }
        if (!GridPlacementValidator.canPlace(target.get().inventory(), moved, targetX, targetY, rotated, null)) {
            return false;
        }
        target.get().inventory().add(moved, targetX, targetY, rotated);
        gridData.extract(entryId, moved.getCount());
        save();
        saveEquipmentStorage(equipmentSlot, target.get().storage());
        return true;
    }

    public boolean insertCurioIntoEquipmentStorage(String identifier, int index, EquipmentSlot equipmentSlot, String containerId,
                                                  int targetX, int targetY, boolean rotated) {
        return insertCurioIntoEquipmentStorage(identifier, index, equipmentSlot, containerId, targetX, targetY, rotated, false);
    }

    public boolean insertCurioIntoEquipmentStorage(String identifier, int index, EquipmentSlot equipmentSlot, String containerId,
                                                  int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (!playerGrid || (GridEquipmentSlots.isBack(equipmentSlot) && "back".equals(identifier) && index == 0)) {
            return false;
        }
        Optional<ItemStack> source = CuriosIntegration.getCurioStack(playerInventory.player, identifier, index);
        Optional<EquipmentStorageEdit> target = editableEquipmentInventory(equipmentSlot, containerId);
        if (source.isEmpty() || target.isEmpty()) {
            return false;
        }
        ItemStack moved = source.get().copy();
        if (moved.getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(moved)) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(moved, targetFolded);
        }
        if (!GridPlacementValidator.canPlace(target.get().inventory(), moved, targetX, targetY, rotated, null)) {
            return false;
        }
        target.get().inventory().add(moved, targetX, targetY, rotated);
        CuriosIntegration.setCurioStack(playerInventory.player, identifier, index, ItemStack.EMPTY);
        saveEquipmentStorage(equipmentSlot, target.get().storage());
        return true;
    }

    public boolean transferEquipmentEntryIntoGrid(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated) {
        return transferEquipmentEntryIntoGrid(equipmentSlot, containerId, entryId, targetX, targetY, rotated, false);
    }

    public boolean transferEquipmentEntryIntoGrid(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        Optional<EquipmentStorageEdit> source = editableEquipmentInventory(equipmentSlot, containerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = source.flatMap(edit -> edit.inventory().getEntry(entryId));
        if (!playerGrid || source.isEmpty() || entry.isEmpty()) {
            return false;
        }
        ItemStack moved = entry.get().stack().copy();
        if (moved.getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(moved)) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(moved, targetFolded);
        }
        if (!GridPlacementValidator.canPlace(gridData, moved, targetX, targetY, rotated, null)) {
            return false;
        }
        gridData.add(moved, targetX, targetY, rotated);
        source.get().inventory().extract(entryId, moved.getCount());
        saveEquipmentStorage(equipmentSlot, source.get().storage());
        save();
        return true;
    }

    public boolean transferEquipmentEntryBetweenStorages(EquipmentSlot sourceSlot, String sourceContainerId, UUID entryId,
                                                           EquipmentSlot targetSlot, String targetContainerId,
                                                           int targetX, int targetY, boolean rotated) {
        if (!playerGrid) {
            return false;
        }
        if (sourceSlot == targetSlot && sourceContainerId.equals(targetContainerId)) {
            return moveEquipmentEntry(sourceSlot, sourceContainerId, entryId, targetX, targetY, rotated);
        }
        Optional<EquipmentStorageEdit> source = editableEquipmentInventory(sourceSlot, sourceContainerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = source.flatMap(edit -> edit.inventory().getEntry(entryId));
        if (source.isEmpty() || entry.isEmpty()) {
            return false;
        }
        if (sourceSlot == targetSlot) {
            Optional<NamedGridInventoryData> targetContainer = source.get().storage().containers().stream()
                    .filter(container -> container.id().equals(targetContainerId))
                    .findFirst();
            if (targetContainer.isEmpty()
                    || !GridPlacementValidator.canPlace(targetContainer.get().inventory(), entry.get().stack(), targetX, targetY, rotated, null)) {
                return false;
            }
            ItemStack moved = entry.get().stack().copy();
            targetContainer.get().inventory().add(moved, targetX, targetY, rotated);
            source.get().inventory().extract(entryId, moved.getCount());
            saveEquipmentStorage(sourceSlot, source.get().storage());
            return true;
        }
        Optional<EquipmentStorageEdit> target = editableEquipmentInventory(targetSlot, targetContainerId);
        if (target.isEmpty()
                || !GridPlacementValidator.canPlace(target.get().inventory(), entry.get().stack(), targetX, targetY, rotated, null)) {
            return false;
        }
        ItemStack moved = entry.get().stack().copy();
        target.get().inventory().add(moved, targetX, targetY, rotated);
        source.get().inventory().extract(entryId, moved.getCount());
        saveEquipmentStorage(sourceSlot, source.get().storage());
        saveEquipmentStorage(targetSlot, target.get().storage());
        return true;
    }

    public boolean extractEquipmentEntryToPlayerSlot(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int playerSlot, int amount) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(equipmentSlot, containerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = edit.flatMap(storage -> storage.inventory().getEntry(entryId));
        if (entry.isEmpty() || !mayInsertIntoVanillaSlot(playerSlot, entry.get().stack())) {
            return false;
        }
        ItemStack destination = playerInventory.getItem(playerSlot);
        ItemStack stack = entry.get().stack();
        int amountToMove = Math.min(amount, stack.getCount());
        if (destination.isEmpty()) {
            playerInventory.setItem(playerSlot, edit.get().inventory().extract(entryId, amountToMove));
        } else if (GridItemStacks.sameItemSameData(destination, stack) && destination.getCount() < destination.getMaxStackSize()) {
            int accepted = Math.min(amountToMove, destination.getMaxStackSize() - destination.getCount());
            edit.get().inventory().extract(entryId, accepted);
            destination.grow(accepted);
        } else {
            return false;
        }
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        playerInventory.setChanged();
        return true;
    }

    private Optional<EquipmentStorageEdit> editableEquipmentInventory(EquipmentSlot slot, String containerId) {
        ItemStack equipped = equipmentStorageStack(slot);
        EquipmentStorageData current = com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().getEquipmentStorage(equipped);
        if ((current == null || current.containers().isEmpty()) && !equipped.isEmpty()) {
            current = EquipmentStorageManager.initializeStorage(equipped, slot);
        }
        if (current == null) {
            return Optional.empty();
        }
        EquipmentStorageData storage = new EquipmentStorageData(current.containers().stream()
                .map(container -> new NamedGridInventoryData(container.id(), container.title(), container.inventory().copy()))
                .toList());
        return storage.containers().stream()
                .filter(container -> container.id().equals(containerId))
                .map(container -> new EquipmentStorageEdit(storage, container.inventory()))
                .findFirst();
    }

    private void saveEquipmentStorage(EquipmentSlot slot, EquipmentStorageData storage) {
        ItemStack equipped = equipmentStorageStack(slot);
        if (equipped.isEmpty()) {
            return;
        }
        com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setEquipmentStorage(equipped, storage);
        if (GridEquipmentSlots.isBack(slot)) {
            CuriosIntegration.setCurioStack(playerInventory.player, "back", 0, equipped);
        }
        playerInventory.setChanged();
        ModNetworking.syncEquipmentStorage(playerInventory.player, slot, storage);
    }

    private ItemStack equipmentStorageStack(EquipmentSlot slot) {
        if (GridEquipmentSlots.isBack(slot)) {
            return CuriosIntegration.getCurioStack(playerInventory.player, "back", 0).orElse(ItemStack.EMPTY);
        }
        return playerInventory.player.getItemBySlot(slot);
    }

    private record EquipmentStorageEdit(EquipmentStorageData storage, GridInventoryData inventory) {
    }

    public boolean extractToPlayerInventory(UUID entryId, int amount) {
        var entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack stack = entry.get().stack().copyWithCount(Math.min(amount, entry.get().stack().getCount()));
        ItemStack probe = stack.copy();
        if (!playerInventory.add(probe)) {
            return false;
        }
        gridData.extract(entryId, stack.getCount());
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean extractToPlayerSlot(UUID entryId, int playerSlot, int amount) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        var entry = gridData.getEntry(entryId);
        if (entry.isEmpty()) {
            return false;
        }
        ItemStack target = playerInventory.getItem(playerSlot);
        ItemStack stack = entry.get().stack();
        if (!mayInsertIntoVanillaSlot(playerSlot, stack)) {
            return false;
        }
        int moveCount = Math.min(amount, stack.getCount());
        if (target.isEmpty()) {
            ItemStack extracted = gridData.extract(entryId, moveCount);
            playerInventory.setItem(playerSlot, extracted);
            playerInventory.setChanged();
            save();
            return true;
        }
        if (!GridItemStacks.sameItemSameData(target, stack) || target.getCount() >= target.getMaxStackSize()) {
            return false;
        }
        int accepted = Math.min(moveCount, target.getMaxStackSize() - target.getCount());
        if (accepted <= 0) {
            return false;
        }
        gridData.extract(entryId, accepted);
        target.grow(accepted);
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean movePlayerFreeSlot(int sourcePlayerSlot, int targetPlayerSlot) {
        if (!playerGrid || sourcePlayerSlot == targetPlayerSlot
                || !isFreePlayerSlot(sourcePlayerSlot) || !isFreePlayerSlot(targetPlayerSlot)) {
            return false;
        }
        Optional<Slot> sourceView = findPlayerSlotView(sourcePlayerSlot);
        Optional<Slot> targetView = findPlayerSlotView(targetPlayerSlot);
        if (sourceView.isEmpty() || targetView.isEmpty() || !sourceView.get().mayPickup(playerInventory.player)) {
            return false;
        }
        ItemStack source = playerInventory.getItem(sourcePlayerSlot);
        ItemStack target = playerInventory.getItem(targetPlayerSlot);
        if (source.isEmpty() || !targetView.get().mayPlace(source)) {
            return false;
        }
        int targetLimit = Math.min(targetView.get().getMaxStackSize(), source.getMaxStackSize());
        if (target.isEmpty()) {
            int moved = Math.min(source.getCount(), targetLimit);
            playerInventory.setItem(targetPlayerSlot, source.copyWithCount(moved));
            source.shrink(moved);
            playerInventory.setChanged();
            return true;
        }
        if (GridItemStacks.sameItemSameData(source, target)) {
            int moved = Math.min(source.getCount(), Math.max(0, targetLimit - target.getCount()));
            if (moved <= 0) {
                return false;
            }
            target.grow(moved);
            source.shrink(moved);
            playerInventory.setChanged();
            return true;
        }
        if (!targetView.get().mayPickup(playerInventory.player) || !sourceView.get().mayPlace(target)) {
            return false;
        }
        int sourceLimit = Math.min(sourceView.get().getMaxStackSize(), target.getMaxStackSize());
        if (source.getCount() > targetLimit || target.getCount() > sourceLimit) {
            return false;
        }
        playerInventory.setItem(sourcePlayerSlot, target);
        playerInventory.setItem(targetPlayerSlot, source);
        playerInventory.setChanged();
        return true;
    }

    public boolean insertPlayerSlotIntoCurio(int sourcePlayerSlot, String identifier, int index) {
        if (!playerGrid) {
            return false;
        }
        return CuriosIntegration.movePlayerSlotToCurio(playerInventory.player, sourcePlayerSlot, identifier, index);
    }

    public boolean insertGridEntryIntoCurio(UUID entryId, String identifier, int index) {
        if (!playerGrid) {
            return false;
        }
        boolean moved = CuriosIntegration.moveGridEntryToCurio(playerInventory.player, gridData, entryId, identifier, index);
        if (moved) {
            save();
        }
        return moved;
    }

    public boolean insertEquipmentStorageEntryIntoCurio(EquipmentSlot sourceSlot, String containerId, UUID entryId, String identifier, int index) {
        if (!playerGrid) {
            return false;
        }
        Optional<EquipmentStorageEdit> source = editableEquipmentInventory(sourceSlot, containerId);
        if (source.isEmpty()) {
            return false;
        }
        boolean moved = CuriosIntegration.moveEquipmentEntryToCurio(playerInventory.player, source.get().inventory(), entryId, identifier, index);
        if (moved) {
            saveEquipmentStorage(sourceSlot, source.get().storage());
        }
        return moved;
    }

    public boolean quickEquipPlayerSlot(int playerSlot) {
        if (!playerGrid || playerSlot < 0 || playerSlot >= playerInventory.getContainerSize() || !isFreePlayerSlot(playerSlot)) {
            return false;
        }
        ItemStack source = playerInventory.getItem(playerSlot);
        if (source.isEmpty()) {
            return false;
        }
        Optional<EquipmentSlot> targetSlot = findEmptyArmorSlot(source);
        if (targetSlot.isPresent()) {
            playerInventory.setItem(equipmentPlayerSlot(targetSlot.get()), source.copyWithCount(1));
            source.shrink(1);
            playerInventory.setChanged();
            return true;
        }
        return CuriosIntegration.quickEquip(playerInventory.player, source);
    }

    public boolean extractCurioToPlayerSlot(String identifier, int index, int targetPlayerSlot) {
        if (!playerGrid || !isFreePlayerSlot(targetPlayerSlot)) {
            return false;
        }
        return CuriosIntegration.moveCurioToPlayerSlot(playerInventory.player, identifier, index, targetPlayerSlot);
    }

    public boolean extractCurioToGrid(String identifier, int index, int targetX, int targetY, boolean rotated) {
        return extractCurioToGrid(identifier, index, targetX, targetY, rotated, false);
    }

    public boolean extractCurioToGrid(String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (!playerGrid) {
            return false;
        }
        boolean moved = CuriosIntegration.moveCurioToGrid(playerInventory.player, gridData, identifier, index, targetX, targetY, rotated, targetFolded);
        if (moved) {
            save();
        }
        return moved;
    }

    private boolean isFreePlayerSlot(int playerSlot) {
        return playerSlot >= 0 && playerSlot <= 8 || playerSlot >= 36 && playerSlot <= 40;
    }

    private Optional<Slot> findPlayerSlotView(int playerSlot) {
        return slots.stream().filter(slot -> slot.getSlotIndex() == playerSlot).findFirst();
    }

    private Optional<EquipmentSlot> findEmptyArmorSlot(ItemStack stack) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (playerInventory.player.getItemBySlot(slot).isEmpty() && EquipmentSlotHelper.canEquip(stack, slot, playerInventory.player)) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    private int equipmentPlayerSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 39;
            case CHEST -> 38;
            case LEGS -> 37;
            case FEET -> 36;
            default -> throw new IllegalArgumentException("Unsupported quick equip slot: " + slot);
        };
    }

    private int equipmentPlayerSlotOrInvalid(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 39;
            case CHEST -> 38;
            case LEGS -> 37;
            case FEET -> 36;
            default -> -1;
        };
    }

    private boolean mayInsertIntoVanillaSlot(int playerSlot, ItemStack stack) {
        return switch (playerSlot) {
            case 39 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.HEAD, playerInventory.player);
            case 38 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.CHEST, playerInventory.player);
            case 37 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.LEGS, playerInventory.player);
            case 36 -> EquipmentSlotHelper.canEquip(stack, EquipmentSlot.FEET, playerInventory.player);
            default -> true;
        };
    }

    public ItemStack bagStack() {
        if (playerGrid) {
            return ItemStack.EMPTY;
        }
        return hand == InteractionHand.MAIN_HAND ? playerInventory.getItem(bagSlot) : playerInventory.player.getOffhandItem();
    }

    public void save() {
        if (playerGrid) {
            GridInventoryServices.playerData().setPlayerGridInventory(playerInventory.player, gridData.copy());
            return;
        }
        ItemStack stack = bagStack();
        if (!stack.isEmpty()) {
            SmallGridBagItem.setData(stack, gridData);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= 0 && index < slots.size()) {
            int playerSlot = slots.get(index).getSlotIndex();
            quickInsertFromPlayerInventory(playerSlot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return playerGrid || !bagStack().isEmpty();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        save();
    }

    private static class EquipmentSlotView extends Slot {
        private final EquipmentSlot equipmentSlot;
        private final Player owner;

        private EquipmentSlotView(Inventory inventory, int index, int x, int y, EquipmentSlot equipmentSlot) {
            super(inventory, index, x, y);
            this.equipmentSlot = equipmentSlot;
            this.owner = inventory.player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return EquipmentSlotHelper.canEquip(stack, equipmentSlot, owner);
        }

        @Override
        public boolean mayPickup(Player player) {
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
