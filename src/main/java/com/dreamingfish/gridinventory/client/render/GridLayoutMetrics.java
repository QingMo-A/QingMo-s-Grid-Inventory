package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;

public final class GridLayoutMetrics {
    public static final int SECTION_GAP = 3;

    private GridLayoutMetrics() {
    }

    public static int width(GridInventoryData inventory, int cell) {
        return inventory.getColumns() * cell + verticalGapCount(inventory, inventory.getColumns()) * SECTION_GAP;
    }

    public static int height(GridInventoryData inventory, int cell) {
        return inventory.getRows() * cell + horizontalGapCount(inventory, inventory.getRows()) * SECTION_GAP;
    }

    public static int cellLeft(GridInventoryData inventory, int x, int cell) {
        return x * cell + verticalGapCount(inventory, x) * SECTION_GAP;
    }

    public static int cellTop(GridInventoryData inventory, int y, int cell) {
        return y * cell + horizontalGapCount(inventory, y) * SECTION_GAP;
    }

    public static int areaWidth(GridInventoryData inventory, int x, int width, int cell) {
        if (width <= 0) {
            return 0;
        }
        return cellLeft(inventory, x + width - 1, cell) + cell - cellLeft(inventory, x, cell);
    }

    public static int areaHeight(GridInventoryData inventory, int y, int height, int cell) {
        if (height <= 0) {
            return 0;
        }
        return cellTop(inventory, y + height - 1, cell) + cell - cellTop(inventory, y, cell);
    }

    public static int cellXAt(GridInventoryData inventory, int relativeX, int cell) {
        return cellAt(relativeX, inventory.getColumns(), cell, boundary -> hasVerticalGapAfter(inventory, boundary));
    }

    public static int cellYAt(GridInventoryData inventory, int relativeY, int cell) {
        return cellAt(relativeY, inventory.getRows(), cell, boundary -> hasHorizontalGapAfter(inventory, boundary));
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
        for (int row = 0; row < inventory.getRows(); row++) {
            String left = inventory.sectionAt(column, row);
            String right = inventory.sectionAt(column + 1, row);
            if (left != null && right != null && !left.equals(right)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHorizontalGapAfter(GridInventoryData inventory, int row) {
        if (!inventory.hasCustomSections() || row < 0 || row >= inventory.getRows() - 1) {
            return false;
        }
        for (int column = 0; column < inventory.getColumns(); column++) {
            String top = inventory.sectionAt(column, row);
            String bottom = inventory.sectionAt(column, row + 1);
            if (top != null && bottom != null && !top.equals(bottom)) {
                return true;
            }
        }
        return false;
    }
}
