package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class GridItemTransferService {
    private GridItemTransferService() {
    }

    public static boolean transfer(GridInventoryMenu menu, GridItemSource source, GridItemTarget target) {
        return transfer(menu, source, target, GridMoveOptions.all(false, false));
    }

    public static boolean transfer(GridInventoryMenu menu, GridItemSource source, GridItemTarget target,
                                   GridMoveOptions options) {
        GridMoveOptions safeOptions = options == null ? GridMoveOptions.all(false, false) : options;
        if (source instanceof GridItemSource.AccessorySlot
                && (target instanceof GridItemTarget.MenuGridPlacement
                || target instanceof GridItemTarget.NestedGridPlacement
                || target instanceof GridItemTarget.NestedEquipmentStoragePlacement
                || target instanceof GridItemTarget.EquipmentStoragePlacement)
                && !menu.isPlayerGrid()) {
            debug(source, target, null, null, false, false, -1, false, false, "not-player-grid");
            return false;
        }
        if (source instanceof GridItemSource.AccessorySlot accessory
                && target instanceof GridItemTarget.PlayerSlot playerSlot) {
            if (!menu.isPlayerGrid()) {
                debug(source, target, null, null, false, false, -1, false, false, "not-player-grid");
                return false;
            }
            boolean moved = menu.extractCurioToPlayerSlot(accessory.identifier(), accessory.index(),
                    playerSlot.slot());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-accessory-player-slot" : "accessory-player-slot-failed");
            return moved;
        }
        if (source instanceof GridItemSource.MenuGridEntry entry
                && target instanceof GridItemTarget.AccessorySlot accessory) {
            if (!menu.isPlayerGrid()) {
                debug(source, target, null, null, false, false, -1, false, false, "not-player-grid");
                return false;
            }
            boolean moved = menu.insertGridEntryIntoCurio(entry.entryId(), accessory.identifier(),
                    accessory.index());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-grid-entry-accessory" : "grid-entry-accessory-failed");
            return moved;
        }
        if (source instanceof GridItemSource.EquipmentStorageEntry entry
                && target instanceof GridItemTarget.AccessorySlot accessory) {
            if (!menu.isPlayerGrid()) {
                debug(source, target, null, null, false, false, -1, false, false, "not-player-grid");
                return false;
            }
            boolean moved = menu.insertEquipmentStorageEntryIntoCurio(entry.slot(), entry.containerId(),
                    entry.entryId(), accessory.identifier(), accessory.index());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-equipment-entry-accessory" : "equipment-entry-accessory-failed");
            return moved;
        }
        if (source instanceof GridItemSource.PlayerSlot playerSlot
                && target instanceof GridItemTarget.AccessorySlot accessory) {
            if (!menu.isPlayerGrid()) {
                debug(source, target, null, null, false, false, -1, false, false, "not-player-grid");
                return false;
            }
            boolean moved = menu.insertPlayerSlotIntoCurio(playerSlot.slot(), accessory.identifier(),
                    accessory.index());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-player-slot-accessory" : "player-slot-accessory-failed");
            return moved;
        }
        if (source instanceof GridItemSource.PlayerSlot sourceSlot
                && target instanceof GridItemTarget.PlayerSlot targetSlot) {
            if (!menu.isPlayerGrid()) {
                debug(source, target, null, null, false, false, -1, false, false, "not-player-grid");
                return false;
            }
            boolean moved = menu.movePlayerFreeSlot(sourceSlot.slot(), targetSlot.slot(),
                    safeOptions.targetFolded());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-player-slot-player-slot" : "player-slot-player-slot-failed");
            return moved;
        }
        if (source instanceof GridItemSource.GroundItem ground
                && target instanceof GridItemTarget.MenuGridPlacement placement) {
            boolean moved = menu.pickupGroundItemIntoGrid(ground.entityId(), placement.x(), placement.y(),
                    placement.rotated());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-ground-item-grid" : "ground-item-grid-failed");
            return moved;
        }
        if (source instanceof GridItemSource.GroundItem ground
                && target instanceof GridItemTarget.NestedGridPlacement placement) {
            boolean moved = menu.pickupGroundItemIntoNestedGrid(ground.entityId(), placement.ownerPath(), "",
                    placement.x(), placement.y(), placement.rotated());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-ground-item-nested-grid" : "ground-item-nested-grid-failed");
            return moved;
        }
        if (source instanceof GridItemSource.GroundItem ground
                && target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            boolean moved = menu.pickupGroundItemIntoNestedGrid(ground.entityId(), placement.ownerPath(),
                    placement.containerId(), placement.x(), placement.y(), placement.rotated());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-ground-item-nested-equipment-storage"
                            : "ground-item-nested-equipment-storage-failed");
            return moved;
        }
        if (source instanceof GridItemSource.GroundItem ground
                && target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            boolean moved = menu.pickupGroundItemIntoEquipmentStorage(ground.entityId(), placement.slot(),
                    placement.containerId(), placement.x(), placement.y(), placement.rotated());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-ground-item-equipment-storage"
                            : "ground-item-equipment-storage-failed");
            return moved;
        }
        if (source instanceof GridItemSource.GroundItem ground
                && target instanceof GridItemTarget.PlayerSlot playerSlot) {
            boolean moved = menu.pickupGroundItemIntoPlayerSlot(ground.entityId(), playerSlot.slot());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-ground-item-player-slot" : "ground-item-player-slot-failed");
            return moved;
        }
        if (source instanceof GridItemSource.GroundItem ground
                && target instanceof GridItemTarget.AccessorySlot accessory) {
            boolean moved = menu.pickupGroundItemIntoCurio(ground.entityId(), accessory.identifier(),
                    accessory.index());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-ground-item-accessory" : "ground-item-accessory-failed");
            return moved;
        }
        if (source instanceof GridItemSource.CreativeItem creative
                && target instanceof GridItemTarget.MenuGridPlacement placement) {
            boolean moved = menu.creativeInsertIntoGrid(creative.tabIndex(), creative.itemIndex(),
                    creative.count(), placement.x(), placement.y(), placement.rotated(), placement.folded());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-creative-item-grid" : "creative-item-grid-failed");
            return moved;
        }
        if (source instanceof GridItemSource.CreativeItem creative
                && target instanceof GridItemTarget.NestedGridPlacement placement) {
            boolean moved = menu.creativeInsertIntoNestedGrid(creative.tabIndex(), creative.itemIndex(),
                    creative.count(), placement.ownerPath(), "", placement.x(), placement.y(),
                    placement.rotated(), placement.folded());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-creative-item-nested-grid" : "creative-item-nested-grid-failed");
            return moved;
        }
        if (source instanceof GridItemSource.CreativeItem creative
                && target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            boolean moved = menu.creativeInsertIntoNestedGrid(creative.tabIndex(), creative.itemIndex(),
                    creative.count(), placement.ownerPath(), placement.containerId(), placement.x(),
                    placement.y(), placement.rotated(), placement.folded());
            debug(source, target, null, null, false, false, -1, moved, moved,
                    moved ? "committed-creative-item-nested-equipment-storage"
                            : "creative-item-nested-equipment-storage-failed");
            return moved;
        }
        Transaction transaction = new Transaction(menu);
        Optional<ResolvedItemRef> sourceRef = transaction.resolveSource(source);
        Optional<ResolvedGridRef> targetRef = transaction.resolveTarget(target);
        if (sourceRef.isEmpty() || targetRef.isEmpty()) {
            debug(source, target, null, null, false, false, -1, false, false, "resolve-failed");
            return false;
        }
        RootKey sourceRoot = sourceRef.get().root.key();
        RootKey targetRoot = targetRef.get().root.key();
        boolean sameRoot = sourceRoot.equals(targetRoot);
        if (source instanceof GridItemSource.AccessorySlot accessory
                && target instanceof GridItemTarget.EquipmentStoragePlacement placement
                && GridEquipmentSlots.isBack(placement.slot())
                && "back".equals(accessory.identifier()) && accessory.index() == 0) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false,
                    targetDepth(target, menu.transactionMenuGridDepth()), false, false,
                    "same-back-accessory-equipment");
            return false;
        }
        if (source instanceof GridItemSource.PlayerSlot playerSlot
                && target instanceof GridItemTarget.EquipmentStoragePlacement placement
                && menu.transactionIsEquipmentPlayerSlot(playerSlot.slot(), placement.slot())) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false,
                    targetDepth(target, menu.transactionMenuGridDepth()), false, false,
                    "same-equipment-player-slot");
            return false;
        }
        boolean ancestorMove = isSelfOrDescendantMove(source, target);
        if (ancestorMove) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, true,
                    targetDepth(target, menu.transactionMenuGridDepth()), false, false, "cycle");
            return false;
        }
        ItemStack moved = GridItemTransferRules.prepareForTarget(sourceRef.get().stackCopy(), target);
        moved = playerSlotPlacementMovedStack(source, target, moved);
        if (moved.isEmpty()) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false,
                    targetDepth(target, menu.transactionMenuGridDepth()), false, false, "empty-source");
            return false;
        }
        if (source instanceof GridItemSource.PlayerSlot
                && target instanceof GridItemTarget.MenuGridPlacement placement) {
            Optional<Boolean> merged = tryMergePlayerSlotIntoMenuGrid(transaction, sourceRef.get(), targetRef.get(),
                    placement, moved, source, target, sourceRoot, targetRoot, sameRoot);
            if (merged.isPresent()) {
                return merged.get();
            }
        }
        UUID ignoredEntryId = sourceRef.get().sameGrid(targetRef.get()) ? sourceRef.get().entryId() : null;
        GridInventoryData validationGrid = targetRef.get().gridCopy().copy();
        if (ignoredEntryId != null) {
            validationGrid.extract(ignoredEntryId, moved.getCount());
        }
        int depth = targetDepth(target, menu.transactionMenuGridDepth());
        boolean canPlace = canPlace(validationGrid, moved, target, ignoredEntryId, menu.transactionMenuGridDepth());
        if (!canPlace) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, false, false, "cannot-place");
            return false;
        }
        if (ignoredEntryId != null) {
            add(validationGrid, moved, target);
            if (!targetRef.get().writeGridToRootCopy(validationGrid)) {
                debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false, "write-same-grid-failed");
                return false;
            }
        } else {
            if (!sourceRef.get().removeFromRootCopy(moved.getCount())) {
                debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false, "remove-source-failed");
                return false;
            }
            targetRef = transaction.resolveTarget(target);
            if (targetRef.isEmpty()) {
                debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false, "target-after-remove-failed");
                return false;
            }
            GridInventoryData targetCopy = targetRef.get().gridCopy().copy();
            if (!canPlace(targetCopy, moved, target, null, menu.transactionMenuGridDepth())) {
                debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false, "cannot-place-after-remove");
                return false;
            }
            add(targetCopy, moved, target);
            if (!targetRef.get().writeGridToRootCopy(targetCopy)) {
                debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false, "write-target-failed");
                return false;
            }
        }
        boolean committed = transaction.commit();
        debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, committed,
                committed ? "committed" : "commit-failed");
        return committed;
    }

    private static ItemStack playerSlotPlacementMovedStack(GridItemSource source, GridItemTarget target,
                                                          ItemStack prepared) {
        if (source instanceof GridItemSource.PlayerSlot
                && (target instanceof GridItemTarget.MenuGridPlacement
                || target instanceof GridItemTarget.EquipmentStoragePlacement)
                && !GridStackMerger.itemsStackableInGrid()
                && prepared.getCount() > 1) {
            return prepared.copyWithCount(1);
        }
        return prepared;
    }

    private static Optional<Boolean> tryMergePlayerSlotIntoMenuGrid(Transaction transaction, ResolvedItemRef sourceRef,
                                                                    ResolvedGridRef targetRef,
                                                                    GridItemTarget.MenuGridPlacement placement,
                                                                    ItemStack moved, GridItemSource source,
                                                                    GridItemTarget target, RootKey sourceRoot,
                                                                    RootKey targetRoot, boolean sameRoot) {
        int depth = targetDepth(target, transaction.menu.transactionMenuGridDepth());
        if (!GridStackMerger.itemsStackableInGrid()) {
            return Optional.empty();
        }
        Optional<GridEntry> targetEntry = targetRef.gridCopy().getEntries().stream()
                .filter(entry -> entry.contains(placement.x(), placement.y()))
                .findFirst();
        if (targetEntry.isEmpty()) {
            return Optional.empty();
        }
        ItemStack existing = targetEntry.get().stack();
        if (!GridItemStacks.sameItemSameData(existing, moved) || existing.getCount() >= existing.getMaxStackSize()) {
            return Optional.empty();
        }
        int moveCount = Math.min(moved.getCount(), existing.getMaxStackSize() - existing.getCount());
        if (moveCount <= 0) {
            return Optional.empty();
        }
        if (!sourceRef.removeFromRootCopy(moveCount)) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false,
                    "merge-remove-source-failed");
            return Optional.of(false);
        }
        GridInventoryData targetCopy = targetRef.gridCopy().copy();
        Optional<GridEntry> copyEntry = targetCopy.getEntries().stream()
                .filter(entry -> entry.entryId().equals(targetEntry.get().entryId()))
                .findFirst();
        if (copyEntry.isEmpty()) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false,
                    "merge-target-entry-missing");
            return Optional.of(false);
        }
        copyEntry.get().stack().grow(moveCount);
        targetCopy.setChanged();
        if (!targetRef.writeGridToRootCopy(targetCopy)) {
            debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, false,
                    "merge-write-target-failed");
            return Optional.of(false);
        }
        boolean committed = transaction.commit();
        debug(source, target, sourceRoot, targetRoot, sameRoot, false, depth, true, committed,
                committed ? "committed-merge" : "merge-commit-failed");
        return Optional.of(committed);
    }

    public static ItemStack prepareForTarget(ItemStack stack, GridItemTarget target) {
        return GridItemTransferRules.prepareForTarget(stack, target);
    }

    public static int targetDepth(GridItemTarget target, int menuGridDepth) {
        return GridItemTransferRules.targetDepth(target, menuGridDepth);
    }

    public static boolean canPlace(GridInventoryData inventory, ItemStack stack, GridItemTarget target,
                                   UUID ignoredEntryId, int menuGridDepth) {
        if (target instanceof GridItemTarget.MenuGridPlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        if (target instanceof GridItemTarget.NestedGridPlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            return GridPlacementValidator.canPlace(inventory, stack, placement.x(), placement.y(), placement.rotated(),
                    ignoredEntryId, targetDepth(target, menuGridDepth));
        }
        return false;
    }

    public static void add(GridInventoryData inventory, ItemStack stack, GridItemTarget target) {
        if (target instanceof GridItemTarget.MenuGridPlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        } else if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        } else if (target instanceof GridItemTarget.NestedGridPlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        } else if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            inventory.add(stack, placement.x(), placement.y(), placement.rotated());
        }
    }

    static Optional<RootKey> classifyRoot(GridItemSource source) {
        if (source instanceof GridItemSource.MenuGridEntry) {
            return Optional.of(RootKey.menuGrid());
        }
        if (source instanceof GridItemSource.PlayerSlot playerSlot) {
            return Optional.of(RootKey.playerSlot(playerSlot.slot()));
        }
        if (source instanceof GridItemSource.AccessorySlot accessory) {
            return Optional.of(RootKey.accessorySlot(accessory.identifier(), accessory.index()));
        }
        if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
            return Optional.of(RootKey.equipmentSlot(entry.slot()));
        }
        if (source instanceof GridItemSource.NestedGridEntry nested) {
            return classifyNestedRoot(nested.ownerPath());
        }
        if (source instanceof GridItemSource.NestedEquipmentStorageEntry nested) {
            return classifyNestedRoot(nested.ownerPath());
        }
        return Optional.empty();
    }

    static Optional<RootKey> classifyRoot(GridItemTarget target) {
        if (target instanceof GridItemTarget.MenuGridPlacement) {
            return Optional.of(RootKey.menuGrid());
        }
        if (target instanceof GridItemTarget.PlayerSlot playerSlot) {
            return Optional.of(RootKey.playerSlot(playerSlot.slot()));
        }
        if (target instanceof GridItemTarget.AccessorySlot accessory) {
            return Optional.of(RootKey.accessorySlot(accessory.identifier(), accessory.index()));
        }
        if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
            return Optional.of(RootKey.equipmentSlot(placement.slot()));
        }
        if (target instanceof GridItemTarget.NestedGridPlacement nested) {
            return classifyNestedRoot(nested.ownerPath());
        }
        if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement nested) {
            return classifyNestedRoot(nested.ownerPath());
        }
        return Optional.empty();
    }

    static boolean sameRoot(GridItemSource source, GridItemTarget target) {
        Optional<RootKey> sourceRoot = classifyRoot(source);
        Optional<RootKey> targetRoot = classifyRoot(target);
        return sourceRoot.isPresent() && sourceRoot.equals(targetRoot);
    }

    static boolean isAncestorPath(NestedContainerPath ancestor, NestedContainerPath child) {
        if (ancestor.segments().size() > child.segments().size()) {
            return false;
        }
        for (int index = 0; index < ancestor.segments().size(); index++) {
            if (!ancestor.segments().get(index).equals(child.segments().get(index))) {
                return false;
            }
        }
        return true;
    }

    static NestedContainerPath relativeToRoot(NestedContainerPath path) {
        Optional<RootKey> root = classifyNestedRoot(path);
        if (root.isEmpty() || path.segments().isEmpty()) {
            return path;
        }
        NestedContainerPath.Segment first = path.segments().get(0);
        if (first instanceof NestedContainerPath.EquipmentEntrySegment) {
            return path;
        }
        return path.tail();
    }

    static boolean isSelfOrDescendantMove(GridItemSource source, GridItemTarget target) {
        Optional<NestedContainerPath> sourceItemPath = sourceItemPath(source);
        Optional<NestedContainerPath> targetOwnerPath = targetOwnerPath(target);
        return sourceItemPath.isPresent()
                && targetOwnerPath.isPresent()
                && isAncestorPath(sourceItemPath.get(), targetOwnerPath.get());
    }

    private static Optional<RootKey> classifyNestedRoot(NestedContainerPath path) {
        if (path.segments().isEmpty()) {
            return Optional.empty();
        }
        NestedContainerPath.Segment first = path.segments().get(0);
        if (first instanceof NestedContainerPath.GridEntrySegment segment) {
            return Optional.of(RootKey.menuGridEntry(segment.entryId()));
        }
        if (first instanceof NestedContainerPath.PlayerSlotSegment segment) {
            return Optional.of(RootKey.playerSlot(segment.slot()));
        }
        if (first instanceof NestedContainerPath.AccessorySegment segment) {
            return Optional.of(RootKey.accessorySlot(segment.identifier(), segment.index()));
        }
        if (first instanceof NestedContainerPath.EquipmentEntrySegment segment) {
            return Optional.of(RootKey.equipmentSlot(segment.slot()));
        }
        return Optional.empty();
    }

    private static Optional<NestedContainerPath> sourceItemPath(GridItemSource source) {
        if (source instanceof GridItemSource.MenuGridEntry entry) {
            return Optional.of(NestedContainerPath.root().gridEntry(entry.entryId()));
        }
        if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
            return Optional.of(NestedContainerPath.root().equipmentEntry(entry.slot(), entry.containerId(), entry.entryId()));
        }
        if (source instanceof GridItemSource.NestedGridEntry entry) {
            return Optional.of(entry.ownerPath().gridEntry(entry.entryId()));
        }
        if (source instanceof GridItemSource.NestedEquipmentStorageEntry entry) {
            return Optional.of(entry.ownerPath().containerEntry(entry.containerId(), entry.entryId()));
        }
        if (source instanceof GridItemSource.PlayerSlot slot) {
            return Optional.of(NestedContainerPath.root().playerSlot(slot.slot()));
        }
        if (source instanceof GridItemSource.AccessorySlot slot) {
            return Optional.of(NestedContainerPath.root().accessory(slot.identifier(), slot.index()));
        }
        return Optional.empty();
    }

    private static Optional<NestedContainerPath> targetOwnerPath(GridItemTarget target) {
        if (target instanceof GridItemTarget.NestedGridPlacement placement) {
            return Optional.of(placement.ownerPath());
        }
        if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
            return Optional.of(placement.ownerPath());
        }
        return Optional.empty();
    }

    private static Optional<GridInventoryData> grid(ItemStack owner, String containerId) {
        if (containerId.isEmpty()) {
            return Optional.ofNullable(GridInventoryServices.itemStackData().getGridInventory(owner)).map(GridInventoryData::copy);
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

    private static boolean writeGrid(ItemStack owner, String containerId, GridInventoryData inventory) {
        if (containerId.isEmpty()) {
            GridInventoryServices.itemStackData().setGridInventory(owner, inventory.copy());
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

    private static void debug(GridItemSource source, GridItemTarget target, RootKey sourceRoot, RootKey targetRoot,
                              boolean sameRoot, boolean ancestorPath, int targetDepth, boolean canPlace,
                              boolean committed, String result) {
        if (!DFGridInventory.LOGGER.isDebugEnabled()) {
            return;
        }
        DFGridInventory.LOGGER.debug(
                "Grid transfer {} source={} target={} sourceRoot={} targetRoot={} sameRoot={} ancestorPath={} targetDepth={} canPlace={} committed={}",
                result, source, target, sourceRoot, targetRoot, sameRoot, ancestorPath, targetDepth, canPlace, committed);
    }

    static final class RootKey {
        enum Kind {
            MENU_GRID,
            PLAYER_SLOT,
            EQUIPMENT_SLOT,
            ACCESSORY_SLOT,
            MENU_GRID_ENTRY
        }

        private final Kind kind;
        private final int slot;
        private final EquipmentSlot equipmentSlot;
        private final String identifier;
        private final UUID entryId;

        private RootKey(Kind kind, int slot, EquipmentSlot equipmentSlot, String identifier, UUID entryId) {
            this.kind = kind;
            this.slot = slot;
            this.equipmentSlot = equipmentSlot;
            this.identifier = identifier;
            this.entryId = entryId;
        }

        static RootKey menuGrid() {
            return new RootKey(Kind.MENU_GRID, -1, null, "", null);
        }

        static RootKey playerSlot(int slot) {
            return new RootKey(Kind.PLAYER_SLOT, slot, null, "", null);
        }

        static RootKey equipmentSlot(EquipmentSlot slot) {
            return new RootKey(Kind.EQUIPMENT_SLOT, -1, slot, "", null);
        }

        static RootKey accessorySlot(String identifier, int index) {
            return new RootKey(Kind.ACCESSORY_SLOT, index, null, identifier, null);
        }

        static RootKey menuGridEntry(UUID entryId) {
            return new RootKey(Kind.MENU_GRID_ENTRY, -1, null, "", entryId);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof RootKey key)) {
                return false;
            }
            return slot == key.slot
                    && kind == key.kind
                    && equipmentSlot == key.equipmentSlot
                    && Objects.equals(identifier, key.identifier)
                    && Objects.equals(entryId, key.entryId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, slot, equipmentSlot, identifier, entryId);
        }

        @Override
        public String toString() {
            return "RootKey[" + kind + ",slot=" + slot + ",equipment=" + equipmentSlot
                    + ",identifier=" + identifier + ",entry=" + entryId + "]";
        }
    }

    private static final class Transaction {
        private final GridInventoryMenu menu;
        private final Map<RootKey, RootState> roots = new LinkedHashMap<>();

        private Transaction(GridInventoryMenu menu) {
            this.menu = menu;
        }

        Optional<ResolvedItemRef> resolveSource(GridItemSource source) {
            if (source instanceof GridItemSource.MenuGridEntry entry) {
                RootState root = root(RootKey.menuGrid());
                return root.gridCopy().flatMap(grid -> grid.getEntry(entry.entryId()))
                        .map(gridEntry -> new ResolvedItemRef(root, NestedContainerPath.root(), "",
                                entry.entryId(), gridEntry.stack().copy()));
            }
            if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
                RootState root = root(RootKey.equipmentSlot(entry.slot()));
                return root.stackCopy().flatMap(stack -> grid(stack, entry.containerId()))
                        .flatMap(grid -> grid.getEntry(entry.entryId()))
                        .map(gridEntry -> new ResolvedItemRef(root, NestedContainerPath.root(), entry.containerId(),
                                entry.entryId(), gridEntry.stack().copy()));
            }
            if (source instanceof GridItemSource.PlayerSlot slot) {
                RootState root = root(RootKey.playerSlot(slot.slot()));
                return root.stackCopy()
                        .filter(stack -> !stack.isEmpty() && menu.transactionMayPickupFreePlayerSlot(slot.slot()))
                        .map(stack -> new ResolvedItemRef(root, NestedContainerPath.root(), "", null, stack.copy()));
            }
            if (source instanceof GridItemSource.AccessorySlot slot) {
                RootState root = root(RootKey.accessorySlot(slot.identifier(), slot.index()));
                return root.stackCopy()
                        .filter(stack -> !stack.isEmpty())
                        .map(stack -> new ResolvedItemRef(root, NestedContainerPath.root(), "", null, stack.copy()));
            }
            if (source instanceof GridItemSource.NestedGridEntry entry) {
                return resolveNestedSource(entry.ownerPath(), "", entry.entryId());
            }
            if (source instanceof GridItemSource.NestedEquipmentStorageEntry entry) {
                return resolveNestedSource(entry.ownerPath(), entry.containerId(), entry.entryId());
            }
            return Optional.empty();
        }

        Optional<ResolvedGridRef> resolveTarget(GridItemTarget target) {
            if (target instanceof GridItemTarget.MenuGridPlacement) {
                RootState root = root(RootKey.menuGrid());
                return root.gridCopy().map(grid -> new ResolvedGridRef(root, NestedContainerPath.root(), "", grid));
            }
            if (target instanceof GridItemTarget.EquipmentStoragePlacement placement) {
                RootState root = root(RootKey.equipmentSlot(placement.slot()));
                return root.stackCopy()
                        .flatMap(stack -> grid(stack, placement.containerId()))
                        .map(grid -> new ResolvedGridRef(root, NestedContainerPath.root(), placement.containerId(), grid));
            }
            if (target instanceof GridItemTarget.NestedGridPlacement placement) {
                return resolveNestedTarget(placement.ownerPath(), "");
            }
            if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement placement) {
                return resolveNestedTarget(placement.ownerPath(), placement.containerId());
            }
            return Optional.empty();
        }

        boolean commit() {
            for (RootState root : roots.values()) {
                if (root.key().kind == RootKey.Kind.MENU_GRID && root.dirty()) {
                    if (root.gridCopy().isEmpty()) {
                        return false;
                    }
                    menu.transactionReplaceMenuGrid(root.gridCopy().get());
                    root.clean();
                }
            }
            for (RootState root : roots.values()) {
                if (root.key().kind != RootKey.Kind.MENU_GRID && root.dirty()) {
                    if (!commitRoot(root)) {
                        return false;
                    }
                    root.clean();
                }
            }
            menu.transactionSave();
            return true;
        }

        private boolean commitRoot(RootState root) {
            RootKey key = root.key();
            Optional<ItemStack> stack = root.stackCopy();
            if (stack.isEmpty()) {
                return false;
            }
            return switch (key.kind) {
                case MENU_GRID_ENTRY -> menu.transactionReplaceMenuGridEntry(key.entryId, stack.get());
                case PLAYER_SLOT -> menu.transactionReplacePlayerSlot(key.slot, stack.get());
                case EQUIPMENT_SLOT -> menu.transactionReplaceEquipmentStack(key.equipmentSlot, stack.get());
                case ACCESSORY_SLOT -> menu.transactionReplaceAccessoryStack(key.identifier, key.slot, stack.get());
                case MENU_GRID -> false;
            };
        }

        private Optional<ResolvedItemRef> resolveNestedSource(NestedContainerPath ownerPath, String containerId, UUID entryId) {
            Optional<RootKey> key = classifyNestedRoot(ownerPath);
            if (key.isEmpty()) {
                return Optional.empty();
            }
            RootState root = root(key.get());
            Optional<NestedContainerAccess.Handle> owner = root.stackCopy()
                    .flatMap(stack -> NestedContainerAccess.resolve(stack, relativeToRoot(ownerPath)));
            Optional<GridEntry> entry = owner.flatMap(handle -> grid(handle.stack(), containerId))
                    .flatMap(grid -> grid.getEntry(entryId));
            return entry.map(gridEntry -> new ResolvedItemRef(root, ownerPath, containerId, entryId,
                    gridEntry.stack().copy()));
        }

        private Optional<ResolvedGridRef> resolveNestedTarget(NestedContainerPath ownerPath, String containerId) {
            Optional<RootKey> key = classifyNestedRoot(ownerPath);
            if (key.isEmpty()) {
                return Optional.empty();
            }
            RootState root = root(key.get());
            Optional<NestedContainerAccess.Handle> owner = root.stackCopy()
                    .flatMap(stack -> NestedContainerAccess.resolve(stack, relativeToRoot(ownerPath)));
            return owner.flatMap(handle -> grid(handle.stack(), containerId))
                    .map(grid -> new ResolvedGridRef(root, ownerPath, containerId, grid));
        }

        private RootState root(RootKey key) {
            return roots.computeIfAbsent(key, ignored -> RootState.snapshot(menu, key));
        }
    }

    private static final class RootState {
        private final RootKey key;
        private GridInventoryData gridCopy;
        private ItemStack stackCopy;
        private boolean dirty;

        private RootState(RootKey key, GridInventoryData gridCopy, ItemStack stackCopy) {
            this.key = key;
            this.gridCopy = gridCopy;
            this.stackCopy = stackCopy;
        }

        static RootState snapshot(GridInventoryMenu menu, RootKey key) {
            return switch (key.kind) {
                case MENU_GRID -> new RootState(key, menu.transactionMenuGridCopy(), ItemStack.EMPTY);
                case MENU_GRID_ENTRY -> new RootState(key, null, menu.transactionMenuGridEntryStack(key.entryId).orElse(ItemStack.EMPTY));
                case PLAYER_SLOT -> new RootState(key, null, menu.transactionPlayerSlotStack(key.slot));
                case EQUIPMENT_SLOT -> new RootState(key, null, menu.transactionEquipmentStack(key.equipmentSlot));
                case ACCESSORY_SLOT -> new RootState(key, null,
                        menu.transactionAccessoryStack(key.identifier, key.slot).orElse(ItemStack.EMPTY));
            };
        }

        RootKey key() {
            return key;
        }

        Optional<GridInventoryData> gridCopy() {
            return Optional.ofNullable(gridCopy).map(GridInventoryData::copy);
        }

        void gridCopy(GridInventoryData updated) {
            this.gridCopy = updated.copy();
            this.dirty = true;
        }

        Optional<ItemStack> stackCopy() {
            return stackCopy == null ? Optional.empty() : Optional.of(stackCopy.copy());
        }

        void stackCopy(ItemStack updated) {
            this.stackCopy = updated.copy();
            this.dirty = true;
        }

        boolean dirty() {
            return dirty;
        }

        void clean() {
            dirty = false;
        }
    }

    private record ResolvedItemRef(RootState root, NestedContainerPath ownerPath, String containerId, UUID entryId,
                                   ItemStack stackCopy) {
        boolean sameGrid(ResolvedGridRef target) {
            return root.key().equals(target.root().key())
                    && ownerPath.equals(target.ownerPath())
                    && containerId.equals(target.containerId())
                    && entryId != null;
        }

        boolean removeFromRootCopy(int amount) {
            if (root.key().kind == RootKey.Kind.MENU_GRID) {
                Optional<GridInventoryData> grid = root.gridCopy();
                if (grid.isEmpty() || entryId == null) {
                    return false;
                }
                GridInventoryData copy = grid.get().copy();
                if (copy.extract(entryId, amount).isEmpty()) {
                    return false;
                }
                root.gridCopy(copy);
                return true;
            }
            if (entryId == null) {
                if (amount <= 0) {
                    return false;
                }
                Optional<ItemStack> rootStack = root.stackCopy();
                if (rootStack.isEmpty() || rootStack.get().isEmpty()) {
                    return false;
                }
                ItemStack updated = rootStack.get().copy();
                int removed = Math.min(amount, updated.getCount());
                if (removed <= 0) {
                    return false;
                }
                updated.shrink(removed);
                root.stackCopy(updated.isEmpty() ? ItemStack.EMPTY : updated);
                return true;
            }
            Optional<ItemStack> rootStack = root.stackCopy();
            if (rootStack.isEmpty()) {
                return false;
            }
            NestedContainerPath relativePath = relativeToRoot(ownerPath);
            Optional<NestedContainerAccess.Handle> owner = NestedContainerAccess.resolve(rootStack.get(), relativePath);
            Optional<GridInventoryData> grid = owner.flatMap(handle -> grid(handle.stack(), containerId));
            if (owner.isEmpty() || grid.isEmpty()) {
                return false;
            }
            GridInventoryData copy = grid.get().copy();
            if (copy.extract(entryId, amount).isEmpty()) {
                return false;
            }
            ItemStack updatedOwner = owner.get().stack().copy();
            if (!writeGrid(updatedOwner, containerId, copy)) {
                return false;
            }
            root.stackCopy(owner.get().write(updatedOwner));
            return true;
        }
    }

    private record ResolvedGridRef(RootState root, NestedContainerPath ownerPath, String containerId,
                                   GridInventoryData gridCopy) {
        boolean writeGridToRootCopy(GridInventoryData updated) {
            if (root.key().kind == RootKey.Kind.MENU_GRID) {
                root.gridCopy(updated);
                return true;
            }
            Optional<ItemStack> rootStack = root.stackCopy();
            if (rootStack.isEmpty()) {
                return false;
            }
            Optional<NestedContainerAccess.Handle> owner = NestedContainerAccess.resolve(rootStack.get(), relativeToRoot(ownerPath));
            if (owner.isEmpty()) {
                return false;
            }
            ItemStack updatedOwner = owner.get().stack().copy();
            if (!writeGrid(updatedOwner, containerId, updated)) {
                return false;
            }
            root.stackCopy(owner.get().write(updatedOwner));
            return true;
        }
    }
}
