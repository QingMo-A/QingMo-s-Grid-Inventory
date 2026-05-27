package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.dreamingfish.gridinventory.common.data.GridCell;
import com.dreamingfish.gridinventory.common.data.GridSection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record EquipmentStorageContainerDefinition(String id, String title, int columns, int rows, List<GridSection> sections,
                                                  List<String> layout, List<String> allowedTags, List<String> blockedTags) {
    public static final Codec<EquipmentStorageContainerDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EquipmentStorageContainerDefinition::id),
            Codec.STRING.fieldOf("title").forGetter(EquipmentStorageContainerDefinition::title),
            Codec.INT.optionalFieldOf("columns", 0).forGetter(EquipmentStorageContainerDefinition::columns),
            Codec.INT.optionalFieldOf("rows", 0).forGetter(EquipmentStorageContainerDefinition::rows),
            GridSection.CODEC.listOf().optionalFieldOf("sections", List.of()).forGetter(EquipmentStorageContainerDefinition::sections),
            Codec.STRING.listOf().optionalFieldOf("layout", List.of()).forGetter(EquipmentStorageContainerDefinition::layout),
            Codec.STRING.listOf().optionalFieldOf("allowed_tags", List.of()).forGetter(EquipmentStorageContainerDefinition::allowedTags),
            Codec.STRING.listOf().optionalFieldOf("blocked_tags", List.of()).forGetter(EquipmentStorageContainerDefinition::blockedTags)
    ).apply(instance, EquipmentStorageContainerDefinition::new));

    public int resolvedColumns() {
        return layout.isEmpty() ? columns : layout.stream().mapToInt(String::length).max().orElse(0);
    }

    public int resolvedRows() {
        return layout.isEmpty() ? rows : layout.size();
    }

    public List<GridSection> resolvedSections() {
        return layout.isEmpty() ? sections : parseLayout(layout);
    }

    private static List<GridSection> parseLayout(List<String> rows) {
        List<GridSection> parsed = new ArrayList<>();
        Set<GridCell> visited = new HashSet<>();
        for (int y = 0; y < rows.size(); y++) {
            String row = rows.get(y);
            for (int x = 0; x < row.length(); x++) {
                char marker = row.charAt(x);
                GridCell start = new GridCell(x, y);
                if (marker == '.' || marker == ' ' || visited.contains(start)) {
                    continue;
                }
                List<GridCell> cells = new ArrayList<>();
                ArrayDeque<GridCell> pending = new ArrayDeque<>();
                pending.add(start);
                visited.add(start);
                while (!pending.isEmpty()) {
                    GridCell cell = pending.removeFirst();
                    cells.add(cell);
                    addAdjacent(rows, marker, cell.x() - 1, cell.y(), visited, pending);
                    addAdjacent(rows, marker, cell.x() + 1, cell.y(), visited, pending);
                    addAdjacent(rows, marker, cell.x(), cell.y() - 1, visited, pending);
                    addAdjacent(rows, marker, cell.x(), cell.y() + 1, visited, pending);
                }
                parsed.add(new GridSection(marker + "_" + parsed.size(), List.copyOf(cells)));
            }
        }
        return List.copyOf(parsed);
    }

    private static void addAdjacent(List<String> rows, char marker, int x, int y, Set<GridCell> visited, ArrayDeque<GridCell> pending) {
        if (y < 0 || y >= rows.size() || x < 0 || x >= rows.get(y).length() || rows.get(y).charAt(x) != marker) {
            return;
        }
        GridCell cell = new GridCell(x, y);
        if (visited.add(cell)) {
            pending.addLast(cell);
        }
    }
}
