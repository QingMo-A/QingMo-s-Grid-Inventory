package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;

public final class GridLayoutMetrics {
    public static final int SECTION_GAP = 3;

    private GridLayoutMetrics() {
    }

    public static int width(GridInventoryData inventory, int cell) {
        if (!inventory.hasCustomSections()) {
            return inventory.getColumns() * cell;
        }
        int max = 0;
        for (int y = 0; y < inventory.getRows(); y++) {
            int right = 0;
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (inventory.isEnabledCell(x, y)) {
                    right = Math.max(right, cellLeft(inventory, x, y, cell) + cell);
                }
            }
            max = Math.max(max, right);
        }
        return max;
    }

    public static int height(GridInventoryData inventory, int cell) {
        if (!inventory.hasCustomSections()) {
            return inventory.getRows() * cell;
        }
        int max = 0;
        for (int x = 0; x < inventory.getColumns(); x++) {
            int bottom = 0;
            for (int y = 0; y < inventory.getRows(); y++) {
                if (inventory.isEnabledCell(x, y)) {
                    bottom = Math.max(bottom, cellTop(inventory, x, y, cell) + cell);
                }
            }
            max = Math.max(max, bottom);
        }
        return max;
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
        int gaps = 0;
        for (int column = 0; column < x; column++) {
            if (hasRowGapAfter(inventory, column, y)) {
                gaps++;
            }
        }
        return x * cell + gaps * SECTION_GAP;
    }

    public static int cellTop(GridInventoryData inventory, int x, int y, int cell) {
        if (!inventory.hasCustomSections()) {
            return y * cell;
        }
        int gaps = 0;
        for (int row = 0; row < y; row++) {
            if (hasColumnGapAfter(inventory, x, row)) {
                gaps++;
            }
        }
        return y * cell + gaps * SECTION_GAP;
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
                int left = cellLeft(inventory, x, y, cell);
                int top = cellTop(inventory, x, y, cell);
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
                int left = cellLeft(inventory, x, y, cell);
                int top = cellTop(inventory, x, y, cell);
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
}
