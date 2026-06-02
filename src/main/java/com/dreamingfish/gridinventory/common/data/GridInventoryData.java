package com.dreamingfish.gridinventory.common.data;

import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.api.IGridInventory;
import com.dreamingfish.gridinventory.common.inventory.GridAutoInsertHelper;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.GridStackMerger;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GridInventoryData implements IGridInventory {
    public static final StreamCodec<RegistryFriendlyByteBuf, GridInventoryData> STREAM_CODEC = StreamCodec.ofMember(GridInventoryData::encode, GridInventoryData::decode);

    public static final Codec<GridInventoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("columns").forGetter(GridInventoryData::getColumns),
            Codec.INT.fieldOf("rows").forGetter(GridInventoryData::getRows),
            GridEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(GridInventoryData::getEntries),
            GridSection.CODEC.listOf().optionalFieldOf("sections", List.of()).forGetter(GridInventoryData::getSections)
    ).apply(instance, GridInventoryData::new));

    private final int columns;
    private final int rows;
    private final List<GridEntry> entries;
    private final List<GridSection> sections;
    private transient Runnable changeListener = () -> {
    };

    public GridInventoryData(int columns, int rows) {
        this(columns, rows, List.of(), List.of());
    }

    public GridInventoryData(int columns, int rows, List<GridEntry> entries) {
        this(columns, rows, entries, List.of());
    }

    public GridInventoryData(int columns, int rows, List<GridEntry> entries, List<GridSection> sections) {
        this.columns = columns;
        this.rows = rows;
        this.entries = new ArrayList<>(entries);
        this.sections = normalizeSections(sections);
    }

    public static GridInventoryData withSections(int columns, int rows, List<GridSection> sections) {
        return new GridInventoryData(columns, rows, List.of(), sections);
    }

    public GridInventoryData copy() {
        return new GridInventoryData(columns, rows, entries.stream()
                .map(entry -> new GridEntry(entry.entryId(), entry.stack().copy(), entry.x(), entry.y(), entry.width(), entry.height(), entry.rotated()))
                .toList(), sections);
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener == null ? () -> {
        } : changeListener;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public List<GridEntry> getEntries() {
        return entries;
    }

    public List<GridSection> getSections() {
        return sections;
    }

    public boolean hasCustomSections() {
        return !sections.isEmpty();
    }

    public boolean isEnabledCell(int x, int y) {
        if (x < 0 || y < 0 || x >= columns || y >= rows) {
            return false;
        }
        return sections.isEmpty() || sections.stream().anyMatch(section -> section.contains(x, y));
    }

    public String sectionAt(int x, int y) {
        if (!isEnabledCell(x, y)) {
            return null;
        }
        if (sections.isEmpty()) {
            return "__rectangle__";
        }
        return sections.stream().filter(section -> section.contains(x, y)).map(GridSection::id).findFirst().orElse(null);
    }

    public boolean canOccupySingleSection(int x, int y, int width, int height) {
        String sectionId = sectionAt(x, y);
        if (sectionId == null) {
            return false;
        }
        for (int cellY = y; cellY < y + height; cellY++) {
            for (int cellX = x; cellX < x + width; cellX++) {
                if (!sectionId.equals(sectionAt(cellX, cellY))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return insert(stack, GridInsertMode.SIMULATE).isEmpty();
    }

    @Override
    public ItemStack insert(ItemStack stack, GridInsertMode mode) {
        if (!GridStackMerger.itemsStackableInGrid()) {
            return insertUnstacked(stack, mode);
        }
        ItemStack remainder = stack;
        if (mode == GridInsertMode.EXECUTE) {
            remainder = GridStackMerger.mergeIntoExisting(this, stack);
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        Optional<GridAutoInsertHelper.Placement> placement = GridAutoInsertHelper.findFirstPlacement(this, remainder);
        if (placement.isEmpty()) {
            return remainder;
        }
        if (mode == GridInsertMode.EXECUTE) {
            GridAutoInsertHelper.Placement target = placement.get();
            add(remainder.copy(), target.x(), target.y(), target.rotated());
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    private ItemStack insertUnstacked(ItemStack stack, GridInsertMode mode) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        GridInventoryData targetInventory = mode == GridInsertMode.EXECUTE ? this : copy();
        ItemStack remainder = stack.copy();
        while (!remainder.isEmpty()) {
            ItemStack single = remainder.copyWithCount(1);
            Optional<GridAutoInsertHelper.Placement> placement = GridAutoInsertHelper.findFirstPlacement(targetInventory, single);
            if (placement.isEmpty()) {
                return remainder;
            }
            GridAutoInsertHelper.Placement target = placement.get();
            targetInventory.add(single, target.x(), target.y(), target.rotated());
            remainder.shrink(1);
        }
        return ItemStack.EMPTY;
    }

    public GridEntry add(ItemStack stack, int x, int y, boolean rotated) {
        var size = GridItemSizeManager.getSize(stack);
        GridEntry entry = new GridEntry(UUID.randomUUID(), stack, x, y, size.placedWidth(rotated), size.placedHeight(rotated), rotated);
        entries.add(entry);
        setChanged();
        return entry;
    }

    public boolean move(UUID entryId, int x, int y, boolean rotated) {
        for (int i = 0; i < entries.size(); i++) {
            GridEntry old = entries.get(i);
            if (old.entryId().equals(entryId)) {
                if (GridStackMerger.itemsStackableInGrid()) {
                    for (int targetIndex = 0; targetIndex < entries.size(); targetIndex++) {
                        GridEntry target = entries.get(targetIndex);
                        if (!target.entryId().equals(entryId)
                                && target.contains(x, y)
                                && ItemStack.isSameItemSameComponents(target.stack(), old.stack())
                                && target.stack().getCount() < target.stack().getMaxStackSize()) {
                            int moved = Math.min(old.stack().getCount(), target.stack().getMaxStackSize() - target.stack().getCount());
                            target.stack().grow(moved);
                            old.stack().shrink(moved);
                            if (old.stack().isEmpty()) {
                                entries.remove(i);
                            }
                            setChanged();
                            return true;
                        }
                    }
                }
                if (!GridPlacementValidator.canPlace(this, old.stack(), x, y, rotated, entryId)) {
                    return false;
                }
                var size = GridItemSizeManager.getSize(old.stack());
                entries.set(i, new GridEntry(entryId, old.stack(), x, y, size.placedWidth(rotated), size.placedHeight(rotated), rotated));
                setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack extract(UUID entryId, int amount) {
        for (int i = 0; i < entries.size(); i++) {
            GridEntry entry = entries.get(i);
            if (entry.entryId().equals(entryId)) {
                ItemStack extracted = entry.stack().copyWithCount(Math.min(amount, entry.stack().getCount()));
                entry.stack().shrink(extracted.getCount());
                if (entry.stack().isEmpty()) {
                    entries.remove(i);
                }
                setChanged();
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public Optional<GridEntry> getEntry(UUID entryId) {
        return entries.stream().filter(entry -> entry.entryId().equals(entryId)).findFirst();
    }

    @Override
    public void setChanged() {
        changeListener.run();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(columns);
        buf.writeVarInt(rows);
        buf.writeVarInt(entries.size());
        for (GridEntry entry : entries) {
            entry.encode(buf);
        }
        buf.writeVarInt(sections.size());
        for (GridSection section : sections) {
            section.encode(buf);
        }
    }

    public static GridInventoryData decode(RegistryFriendlyByteBuf buf) {
        int columns = buf.readVarInt();
        int rows = buf.readVarInt();
        int count = buf.readVarInt();
        List<GridEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entries.add(GridEntry.decode(buf));
        }
        int sectionCount = buf.readVarInt();
        List<GridSection> sections = new ArrayList<>();
        for (int i = 0; i < sectionCount; i++) {
            sections.add(GridSection.decode(buf));
        }
        return new GridInventoryData(columns, rows, entries, sections);
    }

    private static List<GridSection> normalizeSections(List<GridSection> sections) {
        Map<String, List<GridCell>> merged = new LinkedHashMap<>();
        for (GridSection section : sections) {
            merged.computeIfAbsent(canonicalSectionId(section.id()), ignored -> new ArrayList<>()).addAll(section.cells());
        }
        return merged.entrySet().stream()
                .map(entry -> new GridSection(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
    }

    private static String canonicalSectionId(String id) {
        int underscore = id.indexOf('_');
        return underscore > 0 ? id.substring(0, underscore) : id;
    }
}
