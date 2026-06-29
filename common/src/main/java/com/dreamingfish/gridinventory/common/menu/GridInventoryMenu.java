package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.GridExplicitInsertHelper;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridItemTransferService;
import com.dreamingfish.gridinventory.common.inventory.GridStackMerger;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerAccess;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

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
        this(ModMenus.GRID_INVENTORY.get(), containerId, playerInventory, bagSlot, hand, gridData, playerGrid);
    }

    protected GridInventoryMenu(MenuType<?> menuType, int containerId, Inventory playerInventory, int bagSlot,
                                InteractionHand hand, GridInventoryData gridData, boolean playerGrid) {
        super(menuType, containerId);
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

    public int transactionMenuGridDepth() {
        return gridTargetDepth();
    }

    public GridInventoryData transactionMenuGridCopy() {
        return gridData.copy();
    }

    public void transactionReplaceMenuGrid(GridInventoryData data) {
        this.gridData = data.copy();
        this.gridData.setChangeListener(this::save);
    }

    public Optional<ItemStack> transactionMenuGridEntryStack(UUID entryId) {
        return gridData.getEntry(entryId).map(entry -> entry.stack().copy());
    }

    public boolean transactionReplaceMenuGridEntry(UUID entryId, ItemStack stack) {
        for (int index = 0; index < gridData.getEntries().size(); index++) {
            com.dreamingfish.gridinventory.common.data.GridEntry entry = gridData.getEntries().get(index);
            if (entry.entryId().equals(entryId)) {
                gridData.getEntries().set(index, new com.dreamingfish.gridinventory.common.data.GridEntry(
                        entry.entryId(), stack.copy(), entry.x(), entry.y(), entry.width(), entry.height(), entry.rotated()));
                return true;
            }
        }
        return false;
    }

    public boolean transactionIsFreePlayerSlot(int playerSlot) {
        return isFreePlayerSlot(playerSlot);
    }

    public boolean transactionMayPickupFreePlayerSlot(int playerSlot) {
        if (!isFreePlayerSlot(playerSlot)) {
            return false;
        }
        return findPlayerSlotView(playerSlot)
                .map(slot -> slot.mayPickup(playerInventory.player))
                .orElse(false);
    }

    public boolean transactionIsEquipmentPlayerSlot(int playerSlot, EquipmentSlot equipmentSlot) {
        return equipmentPlayerSlotOrInvalid(equipmentSlot) == playerSlot;
    }

    public ItemStack transactionPlayerSlotStack(int playerSlot) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return playerInventory.getItem(playerSlot).copy();
    }

    public boolean transactionReplacePlayerSlot(int playerSlot, ItemStack stack) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        playerInventory.setItem(playerSlot, stack.copy());
        playerInventory.setChanged();
        return true;
    }

    public ItemStack transactionEquipmentStack(EquipmentSlot slot) {
        ItemStack equipped = equipmentStorageStack(slot);
        if (equipped.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = equipped.copy();
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(copy);
        if (storage == null || storage.containers().isEmpty()) {
            EquipmentStorageManager.initializeStorage(copy, slot);
        }
        return copy;
    }

    public boolean transactionReplaceEquipmentStack(EquipmentSlot slot, ItemStack stack) {
        if (GridEquipmentSlots.isBack(slot)) {
            GridInventoryServices.accessories().setAccessoryStack(playerInventory.player, "back", 0, stack.copy());
        } else {
            playerInventory.player.setItemSlot(slot, stack.copy());
        }
        playerInventory.setChanged();
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        if (storage != null) {
            ModNetworking.syncEquipmentStorage(playerInventory.player, slot, storage);
        }
        return true;
    }

    public Optional<ItemStack> transactionAccessoryStack(String identifier, int index) {
        return GridInventoryServices.accessories().getAccessoryStack(playerInventory.player, identifier, index)
                .map(ItemStack::copy);
    }

    public boolean transactionReplaceAccessoryStack(String identifier, int index, ItemStack stack) {
        GridInventoryServices.accessories().setAccessoryStack(playerInventory.player, identifier, index, stack.copy());
        playerInventory.setChanged();
        return true;
    }

    public void transactionSave() {
        save();
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
        if (!GridPlacementValidator.canPlace(gridData, placedStack, targetX, targetY, rotated, null, gridTargetDepth())) {
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
        ItemStack remainder = gridData.insert(source.copy(), GridInsertMode.EXECUTE, gridTargetDepth());
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
        if (!GridPlacementValidator.canPlace(gridData, entry.get().stack(), targetX, targetY, rotated, entryId, gridTargetDepth())) {
            return false;
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
        if (!GridPlacementValidator.canPlace(gridData, toggled, targetX, targetY, rotated, entryId, gridTargetDepth())) {
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
        if (GridInventoryServices.accessories().canQuickEquip(playerInventory.player, entry.get().stack())) {
            ItemStack equipped = gridData.extract(entryId, 1);
            boolean equippedToCurio = GridInventoryServices.accessories().quickEquip(playerInventory.player, equipped);
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
        if (!GridPlacementValidator.canPlace(edit.get().inventory(), placedStack, targetX, targetY, rotated, null, equipmentTargetDepth(equipmentSlot))) {
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
        if (!GridPlacementValidator.canPlace(edit.get().inventory(), entry.get().stack(), targetX, targetY, rotated, entryId, equipmentTargetDepth(equipmentSlot))
                || !edit.get().inventory().move(entryId, targetX, targetY, rotated)) {
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
        if (!GridPlacementValidator.canPlace(edit.get().inventory(), toggled, targetX, targetY, rotated, entryId, equipmentTargetDepth(equipmentSlot))) {
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
        if (GridInventoryServices.accessories().canQuickEquip(playerInventory.player, entry.get().stack())) {
            ItemStack equipped = source.get().inventory().extract(entryId, 1);
            boolean equippedToCurio = GridInventoryServices.accessories().quickEquip(playerInventory.player, equipped);
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
        if (itemEntity.isEmpty() || !insertGroundStackIntoGrid(itemEntity.get(), gridData, targetX, targetY, rotated, gridTargetDepth())) {
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
                || !insertGroundStackIntoGrid(itemEntity.get(), edit.get().inventory(), targetX, targetY, rotated, equipmentTargetDepth(equipmentSlot))) {
            return false;
        }
        saveEquipmentStorage(equipmentSlot, edit.get().storage());
        return true;
    }

    public boolean pickupGroundItemIntoPlayerSlot(int entityId, int playerSlot) {
        if (!(playerInventory.player instanceof ServerPlayer player) || !playerGrid
                || playerSlot < 0 || playerSlot >= playerInventory.getContainerSize() || !isFreePlayerSlot(playerSlot)) {
            return false;
        }
        Optional<ItemEntity> itemEntity = ManualPickupHandler.findReachableItem(player, entityId);
        if (itemEntity.isEmpty()) {
            return false;
        }
        ItemStack groundStack = itemEntity.get().getItem();
        ItemStack current = playerInventory.getItem(playerSlot);
        if (groundStack.isEmpty() || !mayInsertIntoVanillaSlot(playerSlot, groundStack)) {
            return false;
        }
        int effectiveMax = playerSlotStackLimit(playerSlot, current.isEmpty() ? groundStack : current);
        int moved;
        if (current.isEmpty()) {
            moved = Math.min(groundStack.getCount(), effectiveMax);
            if (moved <= 0) {
                return false;
            }
            playerInventory.setItem(playerSlot, groundStack.copyWithCount(moved));
        } else if (GridItemStacks.sameItemSameData(current, groundStack)) {
            moved = Math.min(groundStack.getCount(), effectiveMax - current.getCount());
            if (moved <= 0) {
                return false;
            }
            current.grow(moved);
        } else {
            return false;
        }
        playerInventory.setChanged();
        finishGroundPickup(itemEntity.get(), moved);
        return true;
    }

    public boolean pickupGroundItemIntoCurio(int entityId, String identifier, int index) {
        if (!(playerInventory.player instanceof ServerPlayer player) || !playerGrid) {
            return false;
        }
        Optional<ItemEntity> itemEntity = ManualPickupHandler.findReachableItem(player, entityId);
        if (itemEntity.isEmpty()) {
            return false;
        }
        ItemStack groundStack = itemEntity.get().getItem();
        if (groundStack.isEmpty()) {
            return false;
        }
        int before = groundStack.getCount();
        if (!GridInventoryServices.accessories().insertStackIntoAccessory(playerInventory.player, groundStack, identifier, index)) {
            return false;
        }
        int moved = before - groundStack.getCount();
        if (moved <= 0) {
            return false;
        }
        finishGroundPickup(itemEntity.get(), moved);
        return true;
    }

    public boolean pickupGroundItemIntoNestedGrid(int entityId, NestedContainerPath targetOwnerPath,
                                                  String targetContainerId, int targetX, int targetY,
                                                  boolean rotated) {
        if (!(playerInventory.player instanceof ServerPlayer player)) {
            return false;
        }
        Optional<ItemEntity> itemEntity = ManualPickupHandler.findReachableItem(player, entityId);
        Optional<MenuPathHandle> targetOwner = resolveMenuGridPath(targetOwnerPath);
        Optional<GridInventoryData> targetGrid = targetOwner.flatMap(owner -> nestedGrid(owner.stack(), targetContainerId));
        if (itemEntity.isEmpty() || targetOwner.isEmpty() || targetGrid.isEmpty()) {
            return false;
        }
        ItemStack groundStack = itemEntity.get().getItem();
        int targetDepth = targetOwnerPath.depth() + 1;
        if (groundStack.isEmpty()) {
            return false;
        }
        ItemStack inserted = groundStack.copyWithCount(GridStackMerger.itemsStackableInGrid() ? groundStack.getCount() : 1);
        GridInventoryData targetCopy = targetGrid.get().copy();
        int movedCount = GridExplicitInsertHelper.insertOrMergeAt(
                targetCopy, inserted, targetX, targetY, rotated, targetDepth);
        if (movedCount <= 0) {
            return false;
        }
        ItemStack updatedTargetOwner = targetOwner.get().stack().copy();
        if (!writeNestedGrid(updatedTargetOwner, targetContainerId, targetCopy)
                || !targetOwner.get().write(updatedTargetOwner)) {
            return false;
        }
        finishGroundPickup(itemEntity.get(), movedCount);
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean dropPlayerSlot(int playerSlot) {
        if (!playerGrid || playerSlot < 0 || playerSlot >= playerInventory.getContainerSize() || !isFreePlayerSlot(playerSlot)) {
            return false;
        }
        ItemStack stack = playerInventory.getItem(playerSlot);
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack dropped = stack.copy();
        playerInventory.setItem(playerSlot, ItemStack.EMPTY);
        playerInventory.player.drop(dropped, false);
        playerInventory.setChanged();
        return true;
    }

    private boolean insertGroundStackIntoGrid(ItemEntity itemEntity, GridInventoryData inventory, int targetX, int targetY, boolean rotated, int targetDepth) {
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return false;
        }
        ItemStack inserted = groundStack.copyWithCount(GridStackMerger.itemsStackableInGrid() ? groundStack.getCount() : 1);
        int movedCount = GridExplicitInsertHelper.insertOrMergeAt(
                inventory, inserted, targetX, targetY, rotated, targetDepth);
        if (movedCount <= 0) {
            return false;
        }
        finishGroundPickup(itemEntity, movedCount);
        return true;
    }

    private void finishGroundPickup(ItemEntity itemEntity, int amount) {
        ItemStack groundStack = itemEntity.getItem();
        groundStack.shrink(amount);
        playerInventory.player.take(itemEntity, amount);
        if (groundStack.isEmpty()) {
            itemEntity.discard();
        }
        playerInventory.player.level().playSound(null, playerInventory.player.getX(), playerInventory.player.getY(), playerInventory.player.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                ((playerInventory.player.getRandom().nextFloat() - playerInventory.player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

    public boolean transferGridEntryIntoEquipmentStorage(UUID entryId, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated) {
        return transferGridEntryIntoEquipmentStorage(entryId, equipmentSlot, containerId, targetX, targetY, rotated, false);
    }

    public boolean creativeInsertIntoGrid(int tabIndex, int itemIndex, int count, int targetX, int targetY,
                                          boolean rotated, boolean folded) {
        Optional<ItemStack> stack = creativeStack(tabIndex, itemIndex, count, folded);
        if (stack.isEmpty()) {
            return false;
        }
        int movedCount = GridExplicitInsertHelper.insertOrMergeAt(
                gridData, stack.get(), targetX, targetY, rotated, gridTargetDepth());
        if (movedCount <= 0) {
            return false;
        }
        save();
        return true;
    }

    public boolean creativeInsertIntoEquipmentStorage(int tabIndex, int itemIndex, int count, EquipmentSlot equipmentSlot,
                                                      String containerId, int targetX, int targetY,
                                                      boolean rotated, boolean folded) {
        Optional<ItemStack> stack = creativeStack(tabIndex, itemIndex, count, folded);
        Optional<EquipmentStorageEdit> target = editableEquipmentInventory(equipmentSlot, containerId);
        if (stack.isEmpty() || target.isEmpty()) {
            return false;
        }
        int movedCount = GridExplicitInsertHelper.insertOrMergeAt(target.get().inventory(), stack.get(),
                targetX, targetY, rotated, equipmentTargetDepth(equipmentSlot));
        if (movedCount <= 0) {
            return false;
        }
        saveEquipmentStorage(equipmentSlot, target.get().storage());
        return true;
    }

    public boolean creativeInsertIntoNestedGrid(int tabIndex, int itemIndex, int count, NestedContainerPath targetOwnerPath,
                                                String targetContainerId, int targetX, int targetY,
                                                boolean rotated, boolean folded) {
        Optional<ItemStack> stack = creativeStack(tabIndex, itemIndex, count, folded);
        Optional<MenuPathHandle> targetOwner = resolveMenuGridPath(targetOwnerPath);
        Optional<GridInventoryData> targetGrid = targetOwner.flatMap(owner -> nestedGrid(owner.stack(), targetContainerId));
        if (stack.isEmpty() || targetOwner.isEmpty() || targetGrid.isEmpty()) {
            return false;
        }
        GridInventoryData targetCopy = targetGrid.get().copy();
        int movedCount = GridExplicitInsertHelper.insertOrMergeAt(targetCopy, stack.get(), targetX, targetY,
                rotated, targetOwnerPath.depth() + 1);
        if (movedCount <= 0) {
            return false;
        }
        ItemStack updatedTargetOwner = targetOwner.get().stack().copy();
        if (!writeNestedGrid(updatedTargetOwner, targetContainerId, targetCopy)
                || !targetOwner.get().write(updatedTargetOwner)) {
            return false;
        }
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean creativeInsertIntoPlayerSlot(int tabIndex, int itemIndex, int count, int playerSlot) {
        Optional<ItemStack> stack = creativeStack(tabIndex, itemIndex, count, false);
        if (stack.isEmpty() || playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()
                || !mayInsertIntoVanillaSlot(playerSlot, stack.get())) {
            return false;
        }
        ItemStack current = playerInventory.getItem(playerSlot);
        if (!current.isEmpty() && GridItemStacks.sameItemSameData(current, stack.get())) {
            int effectiveMax = playerSlotStackLimit(playerSlot, current);
            int moved = Math.min(stack.get().getCount(), effectiveMax - current.getCount());
            if (moved <= 0) {
                return false;
            }
            current.grow(moved);
            playerInventory.setChanged();
            return true;
        }
        playerInventory.setItem(playerSlot, stack.get());
        playerInventory.setChanged();
        return true;
    }

    public boolean creativeInsertIntoCurio(int tabIndex, int itemIndex, int count, String identifier, int index) {
        Optional<ItemStack> stack = creativeStack(tabIndex, itemIndex, count, false);
        if (stack.isEmpty()
                || !GridInventoryServices.accessories().insertStackIntoAccessory(playerInventory.player, stack.get(), identifier, index)) {
            return false;
        }
        playerInventory.setChanged();
        save();
        return true;
    }

    private Optional<ItemStack> creativeStack(int tabIndex, int itemIndex, int count, boolean folded) {
        if (!(playerInventory.player instanceof ServerPlayer player) || !player.isCreative() || player.isSpectator()) {
            return Optional.empty();
        }
        CreativeModeTabs.tryRebuildTabContents(player.level().enabledFeatures(), player.hasPermissions(2),
                player.server.registryAccess());
        List<CreativeModeTab> tabs = filteredCreativeTabs();
        if (tabIndex < 0 || tabIndex >= tabs.size()) {
            return Optional.empty();
        }
        CreativeModeTab tab = tabs.get(tabIndex);
        List<ItemStack> items = ((tab == CreativeModeTabs.searchTab() || tab.hasSearchBar())
                ? tab.getSearchTabDisplayItems()
                : tab.getDisplayItems()).stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
        if (itemIndex < 0 || itemIndex >= items.size()) {
            return Optional.empty();
        }
        ItemStack stack = items.get(itemIndex).copy();
        int accepted = Math.max(1, Math.min(count, stack.getMaxStackSize()));
        stack.setCount(accepted);
        if (stack.getItem() instanceof GridBackpackItem) {
            EquipmentStorageManager.initializeStorage(stack, GridEquipmentSlots.back());
            if (folded && !GridBackpackItem.canFold(stack)) {
                return Optional.empty();
            }
            GridInventoryServices.itemStackData().setBackpackFolded(stack, folded);
        }
        return Optional.of(stack);
    }

    private static List<CreativeModeTab> filteredCreativeTabs() {
        return CreativeModeTabs.tabs().stream()
                .filter(tab -> tab != null && tab.shouldDisplay() && tab.hasAnyItems())
                .filter(tab -> !((tab == CreativeModeTabs.searchTab() || tab.hasSearchBar())
                        ? tab.getSearchTabDisplayItems()
                        : tab.getDisplayItems()).stream().filter(stack -> !stack.isEmpty()).toList().isEmpty())
                .toList();
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
        if (!GridPlacementValidator.canPlace(target.get().inventory(), moved, targetX, targetY, rotated, null, equipmentTargetDepth(equipmentSlot))) {
            return false;
        }
        target.get().inventory().add(moved, targetX, targetY, rotated);
        gridData.extract(entryId, moved.getCount());
        save();
        saveEquipmentStorage(equipmentSlot, target.get().storage());
        return true;
    }

    private static GridItemSource nestedSource(NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId) {
        if (sourceContainerId.isEmpty()) {
            return new GridItemSource.NestedGridEntry(sourceOwnerPath, entryId);
        }
        return new GridItemSource.NestedEquipmentStorageEntry(sourceOwnerPath, sourceContainerId, entryId);
    }

    private static GridItemTarget nestedTarget(NestedContainerPath targetOwnerPath, String targetContainerId,
                                               int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (targetContainerId.isEmpty()) {
            return new GridItemTarget.NestedGridPlacement(targetOwnerPath, targetX, targetY, rotated, targetFolded);
        }
        return new GridItemTarget.NestedEquipmentStoragePlacement(targetOwnerPath, targetContainerId,
                targetX, targetY, rotated, targetFolded);
    }

    public boolean transferGridEntryIntoNestedGrid(UUID entryId, NestedContainerPath targetOwnerPath, String targetContainerId,
                                                   int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this, new GridItemSource.MenuGridEntry(entryId),
                nestedTarget(targetOwnerPath, targetContainerId, targetX, targetY, rotated, targetFolded));
    }

    public boolean transferNestedGridEntryIntoGrid(NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId,
                                                   int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this, nestedSource(sourceOwnerPath, sourceContainerId, entryId),
                new GridItemTarget.MenuGridPlacement(targetX, targetY, rotated, targetFolded));
    }

    public boolean transferNestedGridEntryIntoEquipmentStorage(NestedContainerPath sourceOwnerPath, String sourceContainerId,
                                                               UUID entryId, EquipmentSlot equipmentSlot,
                                                               String targetContainerId, int targetX, int targetY,
                                                               boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this, nestedSource(sourceOwnerPath, sourceContainerId, entryId),
                new GridItemTarget.EquipmentStoragePlacement(equipmentSlot, targetContainerId, targetX, targetY,
                        rotated, targetFolded));
    }

    public boolean extractNestedGridEntryToPlayerSlot(NestedContainerPath sourceOwnerPath, String sourceContainerId,
                                                      UUID entryId, int playerSlot, int amount) {
        if (playerSlot < 0 || playerSlot >= playerInventory.getContainerSize()) {
            return false;
        }
        Optional<MenuPathHandle> sourceOwner = resolveMenuGridPath(sourceOwnerPath);
        Optional<GridInventoryData> sourceGrid = sourceOwner.flatMap(owner -> nestedGrid(owner.stack(), sourceContainerId));
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> source = sourceGrid.flatMap(grid -> grid.getEntry(entryId));
        if (sourceOwner.isEmpty() || sourceGrid.isEmpty() || source.isEmpty()) {
            return false;
        }
        ItemStack stack = source.get().stack();
        if (!mayInsertIntoVanillaSlot(playerSlot, stack)) {
            return false;
        }
        ItemStack target = playerInventory.getItem(playerSlot);
        int moveCount = Math.min(amount, stack.getCount());
        int accepted;
        if (target.isEmpty()) {
            accepted = moveCount;
        } else if (GridItemStacks.sameItemSameData(target, stack) && target.getCount() < target.getMaxStackSize()) {
            accepted = Math.min(moveCount, target.getMaxStackSize() - target.getCount());
        } else {
            return false;
        }
        if (accepted <= 0) {
            return false;
        }
        GridInventoryData sourceCopy = sourceGrid.get().copy();
        ItemStack extracted = sourceCopy.extract(entryId, accepted);
        ItemStack updatedSourceOwner = sourceOwner.get().stack().copy();
        if (!writeNestedGrid(updatedSourceOwner, sourceContainerId, sourceCopy) || !sourceOwner.get().write(updatedSourceOwner)) {
            return false;
        }
        if (target.isEmpty()) {
            playerInventory.setItem(playerSlot, extracted);
        } else {
            target.grow(extracted.getCount());
        }
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean dropNestedGridEntry(NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId) {
        Optional<MenuPathHandle> sourceOwner = resolveMenuGridPath(sourceOwnerPath);
        Optional<GridInventoryData> sourceGrid = sourceOwner.flatMap(owner -> nestedGrid(owner.stack(), sourceContainerId));
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> source = sourceGrid.flatMap(grid -> grid.getEntry(entryId));
        if (sourceOwner.isEmpty() || sourceGrid.isEmpty() || source.isEmpty()) {
            return false;
        }
        GridInventoryData sourceCopy = sourceGrid.get().copy();
        ItemStack dropped = sourceCopy.extract(entryId, source.get().stack().getCount());
        if (dropped.isEmpty()) {
            return false;
        }
        ItemStack updatedSourceOwner = sourceOwner.get().stack().copy();
        if (!writeNestedGrid(updatedSourceOwner, sourceContainerId, sourceCopy) || !sourceOwner.get().write(updatedSourceOwner)) {
            return false;
        }
        playerInventory.player.drop(dropped, false);
        playerInventory.setChanged();
        save();
        return true;
    }

    public boolean quickEquipNestedGridEntry(NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId) {
        if (!playerGrid) {
            return false;
        }
        Optional<MenuPathHandle> sourceOwner = resolveMenuGridPath(sourceOwnerPath);
        Optional<GridInventoryData> sourceGrid = sourceOwner.flatMap(owner -> nestedGrid(owner.stack(), sourceContainerId));
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> source = sourceGrid.flatMap(grid -> grid.getEntry(entryId));
        if (sourceOwner.isEmpty() || sourceGrid.isEmpty() || source.isEmpty()) {
            return false;
        }
        ItemStack stack = source.get().stack();
        Optional<EquipmentSlot> targetSlot = findEmptyArmorSlot(stack);
        boolean canEquipToCurio = GridInventoryServices.accessories().canQuickEquip(playerInventory.player, stack);
        if (targetSlot.isEmpty() && !canEquipToCurio) {
            return false;
        }
        GridInventoryData sourceCopy = sourceGrid.get().copy();
        ItemStack equipped = sourceCopy.extract(entryId, 1);
        if (equipped.isEmpty()) {
            return false;
        }
        ItemStack updatedSourceOwner = sourceOwner.get().stack().copy();
        if (!writeNestedGrid(updatedSourceOwner, sourceContainerId, sourceCopy) || !sourceOwner.get().write(updatedSourceOwner)) {
            return false;
        }
        boolean equippedToCurio = false;
        if (targetSlot.isPresent()) {
            playerInventory.setItem(equipmentPlayerSlot(targetSlot.get()), equipped);
        } else {
            equippedToCurio = GridInventoryServices.accessories().quickEquip(playerInventory.player, equipped);
            if (!equippedToCurio) {
                return false;
            }
        }
        playerInventory.setChanged();
        save();
        return targetSlot.isPresent() || equippedToCurio;
    }

    public boolean insertPlayerSlotIntoNestedGrid(int playerSlot, NestedContainerPath targetOwnerPath,
                                                  String targetContainerId, int targetX, int targetY,
                                                  boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this, new GridItemSource.PlayerSlot(playerSlot),
                nestedTarget(targetOwnerPath, targetContainerId, targetX, targetY, rotated, targetFolded));
    }

    public boolean extractCurioToNestedGrid(String identifier, int index, NestedContainerPath targetOwnerPath,
                                            String targetContainerId, int targetX, int targetY,
                                            boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this, new GridItemSource.AccessorySlot(identifier, index),
                nestedTarget(targetOwnerPath, targetContainerId, targetX, targetY, rotated, targetFolded));
    }

    public boolean transferEquipmentEntryIntoNestedGrid(EquipmentSlot sourceSlot, String sourceContainerId, UUID entryId,
                                                        NestedContainerPath targetOwnerPath, String targetContainerId,
                                                        int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this,
                new GridItemSource.EquipmentStorageEntry(sourceSlot, sourceContainerId, entryId),
                nestedTarget(targetOwnerPath, targetContainerId, targetX, targetY, rotated, targetFolded));
    }

    public boolean transferNestedGridEntryIntoNestedGrid(NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId,
                                                         NestedContainerPath targetOwnerPath, String targetContainerId,
                                                         int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return GridItemTransferService.transfer(this, nestedSource(sourceOwnerPath, sourceContainerId, entryId),
                nestedTarget(targetOwnerPath, targetContainerId, targetX, targetY, rotated, targetFolded));
    }

    private Optional<GridInventoryData> nestedGrid(ItemStack owner, String containerId) {
        if (containerId.isEmpty()) {
            return Optional.ofNullable(GridInventoryServices.itemStackData().getGridInventory(owner));
        }
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(owner);
        if (storage == null) {
            return Optional.empty();
        }
        return storage.containers().stream()
                .filter(container -> container.id().equals(containerId))
                .findFirst()
                .map(container -> container.inventory().copy());
    }

    private boolean writeNestedGrid(ItemStack owner, String containerId, GridInventoryData inventory) {
        if (containerId.isEmpty()) {
            GridInventoryServices.itemStackData().setGridInventory(owner, inventory);
            return true;
        }
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(owner);
        if (storage == null) {
            return false;
        }
        boolean[] found = {false};
        EquipmentStorageData updated = new EquipmentStorageData(storage.containers().stream()
                .map(container -> {
                    if (container.id().equals(containerId)) {
                        found[0] = true;
                        return new NamedGridInventoryData(container.id(), container.title(), inventory.copy());
                    }
                    return new NamedGridInventoryData(container.id(), container.title(), container.inventory().copy());
                })
                .toList());
        if (!found[0]) {
            return false;
        }
        GridInventoryServices.itemStackData().setEquipmentStorage(owner, updated);
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
        Optional<ItemStack> source = GridInventoryServices.accessories().getAccessoryStack(playerInventory.player, identifier, index);
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
        if (!GridPlacementValidator.canPlace(target.get().inventory(), moved, targetX, targetY, rotated, null, equipmentTargetDepth(equipmentSlot))) {
            return false;
        }
        target.get().inventory().add(moved, targetX, targetY, rotated);
        GridInventoryServices.accessories().setAccessoryStack(playerInventory.player, identifier, index, ItemStack.EMPTY);
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
        if (!GridPlacementValidator.canPlace(gridData, moved, targetX, targetY, rotated, null, gridTargetDepth())) {
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
        return transferEquipmentEntryBetweenStorages(sourceSlot, sourceContainerId, entryId, targetSlot, targetContainerId,
                targetX, targetY, rotated, false);
    }

    public boolean transferEquipmentEntryBetweenStorages(EquipmentSlot sourceSlot, String sourceContainerId, UUID entryId,
                                                           EquipmentSlot targetSlot, String targetContainerId,
                                                           int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (!playerGrid) {
            return false;
        }
        if (sourceSlot == targetSlot && sourceContainerId.equals(targetContainerId)) {
            return moveEquipmentEntry(sourceSlot, sourceContainerId, entryId, targetX, targetY, rotated, targetFolded);
        }
        Optional<EquipmentStorageEdit> source = editableEquipmentInventory(sourceSlot, sourceContainerId);
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = source.flatMap(edit -> edit.inventory().getEntry(entryId));
        if (source.isEmpty() || entry.isEmpty()) {
            return false;
        }
        ItemStack moved = entry.get().stack().copy();
        if (moved.getItem() instanceof GridBackpackItem) {
            if (targetFolded && !GridBackpackItem.canFold(moved)) {
                return false;
            }
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setBackpackFolded(moved, targetFolded);
        }
        if (sourceSlot == targetSlot) {
            Optional<NamedGridInventoryData> targetContainer = source.get().storage().containers().stream()
                    .filter(container -> container.id().equals(targetContainerId))
                    .findFirst();
            if (targetContainer.isEmpty()
                    || !GridPlacementValidator.canPlace(targetContainer.get().inventory(), moved, targetX, targetY, rotated, null, equipmentTargetDepth(sourceSlot))) {
                return false;
            }
            targetContainer.get().inventory().add(moved, targetX, targetY, rotated);
            source.get().inventory().extract(entryId, moved.getCount());
            saveEquipmentStorage(sourceSlot, source.get().storage());
            return true;
        }
        Optional<EquipmentStorageEdit> target = editableEquipmentInventory(targetSlot, targetContainerId);
        if (target.isEmpty()
                || !GridPlacementValidator.canPlace(target.get().inventory(), moved, targetX, targetY, rotated, null, equipmentTargetDepth(targetSlot))) {
            return false;
        }
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
            GridInventoryServices.accessories().setAccessoryStack(playerInventory.player, "back", 0, equipped);
        }
        playerInventory.setChanged();
        ModNetworking.syncEquipmentStorage(playerInventory.player, slot, storage);
    }

    private int gridTargetDepth() {
        return playerGrid ? 0 : 1;
    }

    private int equipmentTargetDepth(EquipmentSlot slot) {
        return equipmentStorageStack(slot).isEmpty() ? 0 : 1;
    }

    private ItemStack equipmentStorageStack(EquipmentSlot slot) {
        if (GridEquipmentSlots.isBack(slot)) {
            return GridInventoryServices.accessories().getAccessoryStack(playerInventory.player, "back", 0).orElse(ItemStack.EMPTY);
        }
        return playerInventory.player.getItemBySlot(slot);
    }

    private Optional<MenuPathHandle> resolveMenuGridPath(NestedContainerPath path) {
        if (path.segments().isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().get(0) instanceof NestedContainerPath.EquipmentEntrySegment firstEquipment) {
            return resolveEquipmentPath(path, firstEquipment);
        }
        if (path.segments().get(0) instanceof NestedContainerPath.PlayerSlotSegment playerSlot) {
            return resolvePlayerSlotPath(path, playerSlot);
        }
        if (path.segments().get(0) instanceof NestedContainerPath.AccessorySegment accessory) {
            return resolveAccessoryPath(path, accessory);
        }
        if (!(path.segments().get(0) instanceof NestedContainerPath.GridEntrySegment first)) {
            return Optional.empty();
        }
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = gridData.getEntry(first.entryId());
        if (entry.isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().size() == 1) {
            return Optional.of(new MenuPathHandle(entry.get().stack().copy(), updated -> replaceMenuGridEntryStack(first.entryId(), updated)));
        }
        NestedContainerPath tail = path.tail();
        return NestedContainerAccess.resolve(entry.get().stack(), tail)
                .map(handle -> new MenuPathHandle(handle.stack(), updated -> {
                    ItemStack updatedTop = handle.write(updated);
                    return replaceMenuGridEntryStack(first.entryId(), updatedTop);
                }));
    }

    private Optional<MenuPathHandle> resolvePlayerSlotPath(NestedContainerPath path, NestedContainerPath.PlayerSlotSegment first) {
        if (first.slot() < 0 || first.slot() >= playerInventory.getContainerSize()) {
            return Optional.empty();
        }
        ItemStack stack = playerInventory.getItem(first.slot());
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().size() == 1) {
            return Optional.of(new MenuPathHandle(stack.copy(), updated -> replacePlayerSlotStack(first.slot(), updated)));
        }
        return NestedContainerAccess.resolve(stack, path.tail())
                .map(handle -> new MenuPathHandle(handle.stack(), updated -> {
                    ItemStack updatedTop = handle.write(updated);
                    return replacePlayerSlotStack(first.slot(), updatedTop);
                }));
    }

    private Optional<MenuPathHandle> resolveAccessoryPath(NestedContainerPath path, NestedContainerPath.AccessorySegment first) {
        Optional<ItemStack> stack = GridInventoryServices.accessories().getAccessoryStack(playerInventory.player, first.identifier(), first.index());
        if (stack.isEmpty() || stack.get().isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().size() == 1) {
            return Optional.of(new MenuPathHandle(stack.get().copy(),
                    updated -> replaceAccessoryStack(first.identifier(), first.index(), updated)));
        }
        return NestedContainerAccess.resolve(stack.get(), path.tail())
                .map(handle -> new MenuPathHandle(handle.stack(), updated -> {
                    ItemStack updatedTop = handle.write(updated);
                    return replaceAccessoryStack(first.identifier(), first.index(), updatedTop);
                }));
    }

    private Optional<MenuPathHandle> resolveEquipmentPath(NestedContainerPath path, NestedContainerPath.EquipmentEntrySegment first) {
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(first.slot(), first.containerId());
        Optional<com.dreamingfish.gridinventory.common.data.GridEntry> entry = edit.flatMap(value -> value.inventory().getEntry(first.entryId()));
        if (edit.isEmpty() || entry.isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().size() == 1) {
            return Optional.of(new MenuPathHandle(entry.get().stack().copy(),
                    updated -> replaceEquipmentStorageEntryStack(first.slot(), first.containerId(), first.entryId(), updated)));
        }
        NestedContainerPath tail = path.tail();
        return NestedContainerAccess.resolve(entry.get().stack(), tail)
                .map(handle -> new MenuPathHandle(handle.stack(), updated -> {
                    ItemStack updatedTop = handle.write(updated);
                    return replaceEquipmentStorageEntryStack(first.slot(), first.containerId(), first.entryId(), updatedTop);
                }));
    }

    private boolean replaceMenuGridEntryStack(UUID entryId, ItemStack stack) {
        for (int index = 0; index < gridData.getEntries().size(); index++) {
            com.dreamingfish.gridinventory.common.data.GridEntry entry = gridData.getEntries().get(index);
            if (entry.entryId().equals(entryId)) {
                gridData.getEntries().set(index, new com.dreamingfish.gridinventory.common.data.GridEntry(
                        entry.entryId(), stack.copy(), entry.x(), entry.y(), entry.width(), entry.height(), entry.rotated()));
                gridData.setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean replaceEquipmentStorageEntryStack(EquipmentSlot slot, String containerId, UUID entryId, ItemStack stack) {
        Optional<EquipmentStorageEdit> edit = editableEquipmentInventory(slot, containerId);
        if (edit.isEmpty()) {
            return false;
        }
        for (int index = 0; index < edit.get().inventory().getEntries().size(); index++) {
            com.dreamingfish.gridinventory.common.data.GridEntry entry = edit.get().inventory().getEntries().get(index);
            if (entry.entryId().equals(entryId)) {
                edit.get().inventory().getEntries().set(index, new com.dreamingfish.gridinventory.common.data.GridEntry(
                        entry.entryId(), stack.copy(), entry.x(), entry.y(), entry.width(), entry.height(), entry.rotated()));
                edit.get().inventory().setChanged();
                saveEquipmentStorage(slot, edit.get().storage());
                return true;
            }
        }
        return false;
    }

    private boolean replacePlayerSlotStack(int slotIndex, ItemStack stack) {
        if (slotIndex < 0 || slotIndex >= playerInventory.getContainerSize()) {
            return false;
        }
        playerInventory.setItem(slotIndex, stack.copy());
        playerInventory.setChanged();
        return true;
    }

    private boolean replaceAccessoryStack(String identifier, int index, ItemStack stack) {
        GridInventoryServices.accessories().setAccessoryStack(playerInventory.player, identifier, index, stack.copy());
        return true;
    }

    private static boolean ownsTopLevelEntry(NestedContainerPath path, UUID entryId) {
        return !path.segments().isEmpty()
                && path.segments().get(0) instanceof NestedContainerPath.GridEntrySegment segment
                && segment.entryId().equals(entryId);
    }

    private static boolean ownsNestedEntry(NestedContainerPath targetOwnerPath, String targetContainerId,
                                           NestedContainerPath sourceOwnerPath, String sourceContainerId, UUID entryId) {
        if (!startsWith(targetOwnerPath, sourceOwnerPath)) {
            return false;
        }
        if (targetOwnerPath.segments().size() == sourceOwnerPath.segments().size()) {
            return false;
        }
        NestedContainerPath.Segment next = targetOwnerPath.segments().get(sourceOwnerPath.segments().size());
        if (sourceContainerId.isEmpty() && next instanceof NestedContainerPath.GridEntrySegment segment) {
            return segment.entryId().equals(entryId);
        }
        if (!sourceContainerId.isEmpty() && next instanceof NestedContainerPath.ContainerEntrySegment segment) {
            return sourceContainerId.equals(segment.containerId()) && segment.entryId().equals(entryId);
        }
        return false;
    }

    private static boolean startsWith(NestedContainerPath path, NestedContainerPath prefix) {
        if (prefix.segments().size() > path.segments().size()) {
            return false;
        }
        for (int index = 0; index < prefix.segments().size(); index++) {
            if (!path.segments().get(index).equals(prefix.segments().get(index))) {
                return false;
            }
        }
        return true;
    }

    private record MenuPathHandle(ItemStack stack, java.util.function.Function<ItemStack, Boolean> writer) {
        boolean write(ItemStack updated) {
            return writer.apply(updated);
        }
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
        return movePlayerFreeSlot(sourcePlayerSlot, targetPlayerSlot, false);
    }

    public boolean movePlayerFreeSlot(int sourcePlayerSlot, int targetPlayerSlot, boolean targetFolded) {
        if (!playerGrid || !isFreePlayerSlot(sourcePlayerSlot) || !isFreePlayerSlot(targetPlayerSlot)) {
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
        if (sourcePlayerSlot == targetPlayerSlot) {
            if (!applyBackpackFolded(source, targetFolded)) {
                return false;
            }
            playerInventory.setChanged();
            return true;
        }
        int targetLimit = Math.min(targetView.get().getMaxStackSize(), source.getMaxStackSize());
        if (target.isEmpty()) {
            int moved = Math.min(source.getCount(), targetLimit);
            ItemStack movedStack = source.copyWithCount(moved);
            if (!applyBackpackFolded(movedStack, targetFolded)) {
                return false;
            }
            playerInventory.setItem(targetPlayerSlot, movedStack);
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
        ItemStack movedSource = source.copy();
        if (!applyBackpackFolded(movedSource, targetFolded)) {
            return false;
        }
        playerInventory.setItem(sourcePlayerSlot, target);
        playerInventory.setItem(targetPlayerSlot, movedSource);
        playerInventory.setChanged();
        return true;
    }

    private static boolean applyBackpackFolded(ItemStack stack, boolean folded) {
        if (!(stack.getItem() instanceof GridBackpackItem)) {
            return true;
        }
        if (folded && !GridBackpackItem.canFold(stack)) {
            return false;
        }
        GridInventoryServices.itemStackData().setBackpackFolded(stack, folded);
        return true;
    }

    public boolean insertPlayerSlotIntoCurio(int sourcePlayerSlot, String identifier, int index) {
        if (!playerGrid) {
            return false;
        }
        return GridInventoryServices.accessories().movePlayerSlotToAccessory(playerInventory.player, sourcePlayerSlot, identifier, index);
    }

    public boolean insertGridEntryIntoCurio(UUID entryId, String identifier, int index) {
        if (!playerGrid) {
            return false;
        }
        boolean moved = GridInventoryServices.accessories().moveGridEntryToAccessory(playerInventory.player, gridData, entryId, identifier, index);
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
        boolean moved = GridInventoryServices.accessories().moveEquipmentEntryToAccessory(playerInventory.player, source.get().inventory(), entryId, identifier, index);
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
        return GridInventoryServices.accessories().quickEquip(playerInventory.player, source);
    }

    public boolean extractCurioToPlayerSlot(String identifier, int index, int targetPlayerSlot) {
        if (!playerGrid || !isFreePlayerSlot(targetPlayerSlot)) {
            return false;
        }
        return GridInventoryServices.accessories().moveAccessoryToPlayerSlot(playerInventory.player, identifier, index, targetPlayerSlot);
    }

    public boolean dropCurio(String identifier, int index) {
        if (!playerGrid) {
            return false;
        }
        Optional<ItemStack> stack = GridInventoryServices.accessories().getAccessoryStack(playerInventory.player, identifier, index);
        if (stack.isEmpty() || stack.get().isEmpty()) {
            return false;
        }
        GridInventoryServices.accessories().setAccessoryStack(playerInventory.player, identifier, index, ItemStack.EMPTY);
        playerInventory.player.drop(stack.get().copy(), false);
        playerInventory.setChanged();
        return true;
    }

    public boolean extractCurioToGrid(String identifier, int index, int targetX, int targetY, boolean rotated) {
        return extractCurioToGrid(identifier, index, targetX, targetY, rotated, false);
    }

    public boolean extractCurioToGrid(String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        if (!playerGrid) {
            return false;
        }
        boolean moved = GridInventoryServices.accessories().moveAccessoryToGrid(playerInventory.player, gridData, identifier, index, targetX, targetY, rotated, targetFolded);
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

    private int playerSlotStackLimit(int playerSlot, ItemStack stack) {
        int slotLimit = findPlayerSlotView(playerSlot)
                .map(Slot::getMaxStackSize)
                .orElse(playerInventory.getMaxStackSize());
        return Math.min(slotLimit, Math.min(stack.getMaxStackSize(), playerInventory.getMaxStackSize()));
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
