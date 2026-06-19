package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class NestedContainerAccess {
    private NestedContainerAccess() {
    }

    public static Optional<Handle> resolve(ItemStack root, NestedContainerPath path) {
        if (root.isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().isEmpty()) {
            return Optional.of(new Handle(root.copy(), path, Function.identity()));
        }
        return resolveSegment(root.copy(), path, 0, Function.identity());
    }

    public static Optional<ItemStack> update(ItemStack root, NestedContainerPath path, Function<ItemStack, ItemStack> updater) {
        Optional<Handle> handle = resolve(root, path);
        if (handle.isEmpty()) {
            return Optional.empty();
        }
        ItemStack updated = updater.apply(handle.get().stack().copy());
        if (updated == null || updated.isEmpty()) {
            return Optional.empty();
        }
        ItemStack written = handle.get().write(updated);
        if (written.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(written);
    }

    public static Optional<GridInventoryData> grid(ItemStack stack) {
        return Optional.ofNullable(GridInventoryServices.itemStackData().getGridInventory(stack)).map(GridInventoryData::copy);
    }

    public static Optional<EquipmentStorageData> equipmentStorage(ItemStack stack) {
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        if (storage == null || storage.containers().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(copyStorage(storage));
    }

    public static boolean hasOpenableContainer(ItemStack stack) {
        return grid(stack).isPresent() || equipmentStorage(stack).isPresent();
    }

    private static Optional<Handle> resolveSegment(ItemStack current, NestedContainerPath path, int index,
                                                   Function<ItemStack, ItemStack> writer) {
        NestedContainerPath.Segment segment = path.segments().get(index);
        Optional<EntryEdit> edit = entryEdit(current, segment);
        if (edit.isEmpty()) {
            return Optional.empty();
        }
        Function<ItemStack, ItemStack> nextWriter = updatedChild -> writer.apply(edit.get().write(updatedChild));
        if (index == path.segments().size() - 1) {
            return Optional.of(new Handle(edit.get().stack().copy(), path, nextWriter));
        }
        return resolveSegment(edit.get().stack().copy(), path, index + 1, nextWriter);
    }

    private static Optional<EntryEdit> entryEdit(ItemStack stack, NestedContainerPath.Segment segment) {
        if (segment instanceof NestedContainerPath.GridEntrySegment gridSegment) {
            GridInventoryData grid = GridInventoryServices.itemStackData().getGridInventory(stack);
            if (grid == null) {
                return Optional.empty();
            }
            return grid.getEntry(gridSegment.entryId())
                    .map(entry -> new EntryEdit(entry.stack().copy(), updated -> {
                        GridInventoryData copy = replaceGridEntryStack(grid, entry, updated);
                        ItemStack owner = stack.copy();
                        GridInventoryServices.itemStackData().setGridInventory(owner, copy);
                        return owner;
                    }));
        }
        if (segment instanceof NestedContainerPath.EquipmentEntrySegment equipmentSegment) {
            return equipmentEntryEdit(stack, equipmentSegment.containerId(), equipmentSegment.entryId());
        }
        if (segment instanceof NestedContainerPath.ContainerEntrySegment containerSegment) {
            return equipmentEntryEdit(stack, containerSegment.containerId(), containerSegment.entryId());
        }
        return Optional.empty();
    }

    private static Optional<EntryEdit> equipmentEntryEdit(ItemStack stack, String containerId, java.util.UUID entryId) {
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        if (storage == null) {
            return Optional.empty();
        }
        Optional<NamedGridInventoryData> container = storage.containers().stream()
                .filter(candidate -> candidate.id().equals(containerId))
                .findFirst();
        if (container.isEmpty()) {
            return Optional.empty();
        }
        return container.get().inventory().getEntry(entryId)
                .map(entry -> new EntryEdit(entry.stack().copy(), updated -> {
                    EquipmentStorageData copy = replaceEquipmentEntryStack(storage, container.get(), entry, updated);
                    ItemStack owner = stack.copy();
                    GridInventoryServices.itemStackData().setEquipmentStorage(owner, copy);
                    return owner;
                }));
    }

    private static GridInventoryData replaceGridEntryStack(GridInventoryData grid, GridEntry target, ItemStack stack) {
        return new GridInventoryData(grid.getColumns(), grid.getRows(), grid.getEntries().stream()
                .map(entry -> entry.entryId().equals(target.entryId())
                        ? new GridEntry(entry.entryId(), stack.copy(), entry.x(), entry.y(), entry.width(), entry.height(), entry.rotated())
                        : new GridEntry(entry.entryId(), entry.stack().copy(), entry.x(), entry.y(), entry.width(), entry.height(), entry.rotated()))
                .toList(), grid.getSections());
    }

    private static EquipmentStorageData replaceEquipmentEntryStack(EquipmentStorageData storage, NamedGridInventoryData targetContainer,
                                                                   GridEntry target, ItemStack stack) {
        return new EquipmentStorageData(storage.containers().stream()
                .map(container -> container.id().equals(targetContainer.id())
                        ? new NamedGridInventoryData(container.id(), container.title(),
                        replaceGridEntryStack(container.inventory(), target, stack))
                        : new NamedGridInventoryData(container.id(), container.title(), container.inventory().copy()))
                .toList());
    }

    private static EquipmentStorageData copyStorage(EquipmentStorageData storage) {
        return new EquipmentStorageData(storage.containers().stream()
                .map(container -> new NamedGridInventoryData(container.id(), container.title(), container.inventory().copy()))
                .toList());
    }

    public record Handle(ItemStack stack, NestedContainerPath path, Function<ItemStack, ItemStack> writer) {
        public ItemStack write(ItemStack updated) {
            return writer.apply(updated);
        }

        public Optional<GridInventoryData> grid() {
            return NestedContainerAccess.grid(stack);
        }

        public Optional<EquipmentStorageData> equipmentStorage() {
            return NestedContainerAccess.equipmentStorage(stack);
        }
    }

    private record EntryEdit(ItemStack stack, Function<ItemStack, ItemStack> writer) {
        ItemStack write(ItemStack updated) {
            return writer.apply(updated);
        }
    }
}
