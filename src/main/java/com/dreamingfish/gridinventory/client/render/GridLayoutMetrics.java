package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.GridSection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class GridLayoutMetrics {
    public static final int SECTION_GAP = 3;
    private static final Map<GridInventoryData, Map<Integer, Layout>> CACHE = new WeakHashMap<>();

    private GridLayoutMetrics() {
    }

    public static int width(GridInventoryData inventory, int cell) {
        if (!inventory.hasCustomSections()) {
            return inventory.getColumns() * cell;
        }
        return layout(inventory, cell).width();
    }

    public static int height(GridInventoryData inventory, int cell) {
        if (!inventory.hasCustomSections()) {
            return inventory.getRows() * cell;
        }
        return layout(inventory, cell).height();
    }

    public static int cellLeft(GridInventoryData inventory, int x, int cell) {
        return x * cell;
    }

    public static int cellTop(GridInventoryData inventory, int y, int cell) {
        return y * cell;
    }

    public static int cellLeft(GridInventoryData inventory, int x, int y, int cell) {
        if (!inventory.hasCustomSections()) {
            return x * cell;
        }
        return layout(inventory, cell).left(x, y);
    }

    private static int computeCellLeft(GridInventoryData inventory, int x, int y, int cell) {
        String section = inventory.sectionAt(x, y);
        GridSection gridSection = sectionById(inventory, section);
        if (gridSection == null) {
            return rowOffset(inventory, y, cell) + rowCellLeft(inventory, x, y, cell);
        }
        int anchorX = anchorCellX(gridSection);
        int anchorY = anchorCellY(gridSection);
        int sectionX = centeredInteriorSectionX(inventory, gridSection, anchorY, cell);
        if (sectionX == Integer.MIN_VALUE) {
            sectionX = rowOffset(inventory, anchorY, cell) + rowCellLeft(inventory, anchorX, anchorY, cell);
        }
        return sectionX + localColumnIndex(gridSection, x, y) * cell;
    }

    public static int cellTop(GridInventoryData inventory, int x, int y, int cell) {
        if (!inventory.hasCustomSections()) {
            return y * cell;
        }
        return layout(inventory, cell).top(x, y);
    }

    private static int computeCellTop(GridInventoryData inventory, int x, int y, int cell) {
        GridSection section = sectionById(inventory, inventory.sectionAt(x, y));
        if (section == null) {
            return y * cell;
        }
        return sectionAnchorY(inventory, section, cell, new HashSet<>()) + localRowIndex(section, y) * cell;
    }

    public static int areaWidth(GridInventoryData inventory, int x, int width, int cell) {
        if (width <= 0) {
            return 0;
        }
        return cellLeft(inventory, x + width - 1, cell) + cell - cellLeft(inventory, x, cell);
    }

    public static int areaWidth(GridInventoryData inventory, int x, int y, int width, int cell) {
        if (width <= 0) {
            return 0;
        }
        if (inventory.hasCustomSections()) {
            return layout(inventory, cell).left(x + width - 1, y) + cell - layout(inventory, cell).left(x, y);
        }
        return cellLeft(inventory, x + width - 1, y, cell) + cell - cellLeft(inventory, x, y, cell);
    }

    public static int areaHeight(GridInventoryData inventory, int y, int height, int cell) {
        if (height <= 0) {
            return 0;
        }
        return cellTop(inventory, y + height - 1, cell) + cell - cellTop(inventory, y, cell);
    }

    public static int areaHeight(GridInventoryData inventory, int x, int y, int height, int cell) {
        if (height <= 0) {
            return 0;
        }
        if (inventory.hasCustomSections()) {
            return layout(inventory, cell).top(x, y + height - 1) + cell - layout(inventory, cell).top(x, y);
        }
        return cellTop(inventory, x, y + height - 1, cell) + cell - cellTop(inventory, x, y, cell);
    }

    public static int cellXAt(GridInventoryData inventory, int relativeX, int cell) {
        return cellAt(relativeX, inventory.getColumns(), cell, boundary -> hasVerticalGapAfter(inventory, boundary));
    }

    public static int cellYAt(GridInventoryData inventory, int relativeY, int cell) {
        return cellAt(relativeY, inventory.getRows(), cell, boundary -> hasHorizontalGapAfter(inventory, boundary));
    }

    public static int cellXAt(GridInventoryData inventory, int relativeX, int relativeY, int cell) {
        if (!inventory.hasCustomSections()) {
            return cellXAt(inventory, relativeX, cell);
        }
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (!inventory.isEnabledCell(x, y)) {
                    continue;
                }
                Layout layout = layout(inventory, cell);
                int left = layout.left(x, y);
                int top = layout.top(x, y);
                if (relativeX >= left && relativeX < left + cell && relativeY >= top && relativeY < top + cell) {
                    return x;
                }
            }
        }
        return -1;
    }

    public static int cellYAt(GridInventoryData inventory, int relativeX, int relativeY, int cell) {
        if (!inventory.hasCustomSections()) {
            return cellYAt(inventory, relativeY, cell);
        }
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (!inventory.isEnabledCell(x, y)) {
                    continue;
                }
                Layout layout = layout(inventory, cell);
                int left = layout.left(x, y);
                int top = layout.top(x, y);
                if (relativeX >= left && relativeX < left + cell && relativeY >= top && relativeY < top + cell) {
                    return y;
                }
            }
        }
        return -1;
    }

    private static int cellAt(int offset, int count, int cell, java.util.function.IntPredicate gapAfter) {
        if (offset < 0) {
            return -1;
        }
        int cursor = 0;
        for (int index = 0; index < count; index++) {
            if (offset >= cursor && offset < cursor + cell) {
                return index;
            }
            cursor += cell;
            if (index < count - 1 && gapAfter.test(index)) {
                if (offset < cursor + SECTION_GAP) {
                    return -1;
                }
                cursor += SECTION_GAP;
            }
        }
        return -1;
    }

    private static int verticalGapCount(GridInventoryData inventory, int beforeColumn) {
        int count = 0;
        for (int column = 0; column < beforeColumn; column++) {
            if (hasVerticalGapAfter(inventory, column)) {
                count++;
            }
        }
        return count;
    }

    private static int horizontalGapCount(GridInventoryData inventory, int beforeRow) {
        int count = 0;
        for (int row = 0; row < beforeRow; row++) {
            if (hasHorizontalGapAfter(inventory, row)) {
                count++;
            }
        }
        return count;
    }

    private static boolean hasVerticalGapAfter(GridInventoryData inventory, int column) {
        if (!inventory.hasCustomSections() || column < 0 || column >= inventory.getColumns() - 1) {
            return false;
        }
        boolean hasDifferentNeighbor = false;
        for (int row = 0; row < inventory.getRows(); row++) {
            String left = inventory.sectionAt(column, row);
            String right = inventory.sectionAt(column + 1, row);
            if (left == null || right == null) {
                continue;
            }
            if (left.equals(right)) {
                return false;
            }
            hasDifferentNeighbor = true;
        }
        return hasDifferentNeighbor;
    }

    private static boolean hasRowGapAfter(GridInventoryData inventory, int column, int row) {
        String left = inventory.sectionAt(column, row);
        String right = inventory.sectionAt(column + 1, row);
        return left != null && right != null && !left.equals(right);
    }

    private static boolean hasHorizontalGapAfter(GridInventoryData inventory, int row) {
        if (!inventory.hasCustomSections() || row < 0 || row >= inventory.getRows() - 1) {
            return false;
        }
        boolean hasDifferentNeighbor = false;
        for (int column = 0; column < inventory.getColumns(); column++) {
            String top = inventory.sectionAt(column, row);
            String bottom = inventory.sectionAt(column, row + 1);
            if (top == null || bottom == null) {
                continue;
            }
            if (top.equals(bottom)) {
                return false;
            }
            hasDifferentNeighbor = true;
        }
        return hasDifferentNeighbor;
    }

    private static boolean hasColumnGapAfter(GridInventoryData inventory, int column, int row) {
        String top = inventory.sectionAt(column, row);
        String bottom = inventory.sectionAt(column, row + 1);
        return top != null && bottom != null && !top.equals(bottom);
    }

    private static int widestRowWidth(GridInventoryData inventory, int cell) {
        int max = 0;
        for (int y = 0; y < inventory.getRows(); y++) {
            max = Math.max(max, rowWidth(inventory, y, cell));
        }
        return max;
    }

    private static int rowWidth(GridInventoryData inventory, int row, int cell) {
        int first = firstEnabledColumn(inventory, row);
        int last = lastEnabledColumn(inventory, row);
        if (first < 0 || last < 0) {
            return 0;
        }
        return rowCellLeft(inventory, last, row, cell) - rowCellLeft(inventory, first, row, cell) + cell;
    }

    private static int rowOffset(GridInventoryData inventory, int row, int cell) {
        int rowWidth = rowWidth(inventory, row, cell);
        if (rowWidth <= 0) {
            return 0;
        }
        return Math.max(0, (widestRowWidth(inventory, cell) - rowWidth) / 2);
    }

    private static int rowCellLeft(GridInventoryData inventory, int x, int y, int cell) {
        int first = firstEnabledColumn(inventory, y);
        if (first < 0) {
            return x * cell;
        }
        int gaps = 0;
        for (int column = first; column < x; column++) {
            if (hasRowGapAfter(inventory, column, y)) {
                gaps++;
            }
        }
        return (x - first) * cell + gaps * SECTION_GAP;
    }

    private static int firstEnabledColumn(GridInventoryData inventory, int row) {
        for (int x = 0; x < inventory.getColumns(); x++) {
            if (inventory.isEnabledCell(x, row)) {
                return x;
            }
        }
        return -1;
    }

    private static int lastEnabledColumn(GridInventoryData inventory, int row) {
        for (int x = inventory.getColumns() - 1; x >= 0; x--) {
            if (inventory.isEnabledCell(x, row)) {
                return x;
            }
        }
        return -1;
    }

    private static int centeredInteriorSectionX(GridInventoryData inventory, GridSection section, int row, int cell) {
        int first = firstEnabledColumn(inventory, row);
        int last = lastEnabledColumn(inventory, row);
        if (first < 0 || last <= first) {
            return Integer.MIN_VALUE;
        }
        String leftSection = inventory.sectionAt(first, row);
        String rightSection = inventory.sectionAt(last, row);
        if (section.id().equals(leftSection) || section.id().equals(rightSection)) {
            return Integer.MIN_VALUE;
        }
        int sectionCellsOnRow = (int) section.cells().stream().filter(gridCell -> gridCell.y() == row).count();
        if (sectionCellsOnRow <= 1) {
            return Integer.MIN_VALUE;
        }
        int firstSectionColumnOnRow = section.cells().stream()
                .filter(gridCell -> gridCell.y() == row)
                .mapToInt(com.dreamingfish.gridinventory.common.data.GridCell::x)
                .min().orElse(-1);
        int lastSectionColumnOnRow = section.cells().stream()
                .filter(gridCell -> gridCell.y() == row)
                .mapToInt(com.dreamingfish.gridinventory.common.data.GridCell::x)
                .max().orElse(-1);
        if (firstSectionColumnOnRow <= first || lastSectionColumnOnRow >= last) {
            return Integer.MIN_VALUE;
        }
        int leftBoundary = rowOffset(inventory, row, cell) + rowCellLeft(inventory, first, row, cell) + cell + SECTION_GAP;
        int rightBoundary = rowOffset(inventory, row, cell) + rowCellLeft(inventory, last, row, cell) - SECTION_GAP;
        int available = rightBoundary - leftBoundary;
        int sectionWidth = sectionCellsOnRow * cell;
        if (available < sectionWidth) {
            return Integer.MIN_VALUE;
        }
        return leftBoundary + (available - sectionWidth) / 2;
    }

    private static GridSection sectionById(GridInventoryData inventory, String id) {
        if (id == null) {
            return null;
        }
        return inventory.getSections().stream().filter(section -> section.id().equals(id)).findFirst().orElse(null);
    }

    private static int anchorCellX(GridSection section) {
        int firstRow = anchorCellY(section);
        return section.cells().stream()
                .filter(cell -> cell.y() == firstRow)
                .mapToInt(com.dreamingfish.gridinventory.common.data.GridCell::x)
                .min().orElse(0);
    }

    private static int anchorCellY(GridSection section) {
        return section.cells().stream()
                .mapToInt(com.dreamingfish.gridinventory.common.data.GridCell::y)
                .min().orElse(0);
    }

    private static int localColumnIndex(GridSection section, int x, int y) {
        int index = 0;
        for (var cell : section.cells()) {
            if (cell.y() == y && cell.x() < x) {
                index++;
            }
        }
        return index;
    }

    private static int localRowIndex(GridSection section, int y) {
        return (int) section.cells().stream()
                .mapToInt(com.dreamingfish.gridinventory.common.data.GridCell::y)
                .distinct()
                .filter(row -> row < y)
                .count();
    }

    private static int sectionAnchorY(GridInventoryData inventory, GridSection section, int cell, Set<String> visiting) {
        if (!visiting.add(section.id())) {
            return anchorCellY(section) * cell;
        }
        int sourceRow = anchorCellY(section);
        int y = rowAnchorY(inventory, sourceRow, cell, visiting);
        visiting.remove(section.id());
        return y;
    }

    private static int rowAnchorY(GridInventoryData inventory, int row, int cell, Set<String> visiting) {
        if (row <= 0) {
            return 0;
        }
        int y = rowAnchorY(inventory, row - 1, cell, visiting) + cell;
        for (int column = 0; column < inventory.getColumns(); column++) {
            String current = inventory.sectionAt(column, row);
            String above = inventory.sectionAt(column, row - 1);
            if (current != null && above != null && !current.equals(above)) {
                GridSection section = sectionById(inventory, current);
                GridSection aboveSection = sectionById(inventory, above);
                if (section != null && aboveSection != null && anchorCellY(section) == row) {
                    int aboveTop = sectionAnchorY(inventory, aboveSection, cell, visiting) + localRowIndex(aboveSection, row - 1) * cell;
                    y = Math.max(y, aboveTop + cell + SECTION_GAP);
                }
            }
        }
        return y;
    }

    private static Layout layout(GridInventoryData inventory, int cell) {
        synchronized (CACHE) {
            return CACHE.computeIfAbsent(inventory, ignored -> new HashMap<>())
                    .computeIfAbsent(cell, ignored -> computeLayout(inventory, cell));
        }
    }

    private static Layout computeLayout(GridInventoryData inventory, int cell) {
        int[][] left = new int[inventory.getRows()][inventory.getColumns()];
        int[][] top = new int[inventory.getRows()][inventory.getColumns()];
        for (int y = 0; y < inventory.getRows(); y++) {
            Arrays.fill(left[y], Integer.MIN_VALUE);
            Arrays.fill(top[y], Integer.MIN_VALUE);
        }
        int width = 0;
        int height = 0;
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (!inventory.isEnabledCell(x, y)) {
                    continue;
                }
                left[y][x] = computeCellLeft(inventory, x, y, cell);
                top[y][x] = computeCellTop(inventory, x, y, cell);
                width = Math.max(width, left[y][x] + cell);
                height = Math.max(height, top[y][x] + cell);
            }
        }
        return new Layout(left, top, width, height);
    }

    private record Layout(int[][] left, int[][] top, int width, int height) {
        int left(int x, int y) {
            return inBounds(left, x, y) ? left[y][x] : Integer.MIN_VALUE;
        }

        int top(int x, int y) {
            return inBounds(top, x, y) ? top[y][x] : Integer.MIN_VALUE;
        }

        private static boolean inBounds(int[][] values, int x, int y) {
            return y >= 0 && y < values.length && x >= 0 && x < values[y].length;
        }
    }

}
