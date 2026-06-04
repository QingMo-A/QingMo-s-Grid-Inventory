package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.dreamingfish.gridinventory.common.data.GridCell;
import com.dreamingfish.gridinventory.common.data.GridSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        rows = normalizeLayoutRows(rows);
        Map<Character, List<GridCell>> parsed = new LinkedHashMap<>();
        for (int y = 0; y < rows.size(); y++) {
            String row = rows.get(y);
            for (int x = 0; x < row.length(); x++) {
                char marker = row.charAt(x);
                if (marker == '.' || marker == ' ') {
                    continue;
                }
                parsed.computeIfAbsent(marker, ignored -> new ArrayList<>()).add(new GridCell(x, y));
            }
        }
        List<GridSection> sections = new ArrayList<>();
        parsed.forEach((marker, cells) -> sections.add(new GridSection(String.valueOf(marker), List.copyOf(cells))));
        return List.copyOf(sections);
    }

    private static List<String> normalizeLayoutRows(List<String> rows) {
        if (rows.isEmpty()) {
            return rows;
        }
        int width = rows.stream().mapToInt(String::length).max().orElse(0);
        boolean explicitBlankCells = rows.stream().anyMatch(row -> row.indexOf('.') >= 0 || row.indexOf(' ') >= 0);
        List<String> normalized = new ArrayList<>();
        for (String row : rows) {
            if (row.length() >= width) {
                normalized.add(row);
                continue;
            }
            if (!explicitBlankCells) {
                String expanded = expandImplicitInteriorGap(row, width, normalized);
                if (expanded != null) {
                    normalized.add(expanded);
                    continue;
                }
            }
            int missing = width - row.length();
            int leftPadding = explicitBlankCells ? 0 : missing / 2;
            int rightPadding = missing - leftPadding;
            normalized.add(".".repeat(leftPadding) + row + ".".repeat(rightPadding));
        }
        return normalized;
    }

    private static String expandImplicitInteriorGap(String row, int width, List<String> previousRows) {
        if (row.length() < 3 || previousRows.isEmpty()) {
            return null;
        }
        String previous = previousRows.get(previousRows.size() - 1);
        char first = row.charAt(0);
        char last = row.charAt(row.length() - 1);
        if (first == '.' || first == ' ' || last == '.' || last == ' ') {
            return null;
        }
        if (previous.length() < width || previous.charAt(0) != first || previous.charAt(width - 1) != last) {
            return null;
        }
        String middle = row.substring(1, row.length() - 1);
        int interiorWidth = width - 2;
        if (middle.length() > interiorWidth) {
            return null;
        }
        int leftInteriorPadding = 0;
        int rightInteriorPadding = interiorWidth - middle.length() - leftInteriorPadding;
        return first + ".".repeat(leftInteriorPadding) + middle + ".".repeat(rightInteriorPadding) + last;
    }

}
