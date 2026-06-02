package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.client.gui.GuiGraphics;

public final class GridRenderer {
    private static final int SECTION_GAP = 3;
    private static final int SECTION_BORDER_WIDTH = 1;
    private static final int SECTION_FILL = 0xFF111111;
    private static final int SECTION_BORDER = 0xFF555555;
    private static final int SECTION_GRID_LINE = 0x332F2F2F;

    private GridRenderer() {
    }

    public static void renderGrid(GuiGraphics graphics, int left, int top, int columns, int rows, int cell) {
        int width = columns * cell;
        int height = rows * cell;
        graphics.fill(left - 1, top - 1, left + width + 1, top + height + 1, 0xFF3F3F3F);
        graphics.fill(left, top, left + width, top + height, 0xFF111111);
        for (int x = 0; x <= columns; x++) {
            graphics.fill(left + x * cell, top, left + x * cell + 1, top + height, 0xFF555555);
        }
        for (int y = 0; y <= rows; y++) {
            graphics.fill(left, top + y * cell, left + width, top + y * cell + 1, 0xFF555555);
        }
    }

    public static void renderGrid(GuiGraphics graphics, int left, int top, GridInventoryData inventory, int cell) {
        if (!inventory.hasCustomSections()) {
            renderGrid(graphics, left, top, inventory.getColumns(), inventory.getRows(), cell);
            return;
        }
        graphics.fill(left, top, left + GridLayoutMetrics.width(inventory, cell), top + GridLayoutMetrics.height(inventory, cell), 0x00111111);
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (!inventory.isEnabledCell(x, y)) {
                    continue;
                }
                int cellLeft = left + GridLayoutMetrics.cellLeft(inventory, x, y, cell);
                int cellTop = top + GridLayoutMetrics.cellTop(inventory, x, y, cell);
                graphics.fill(cellLeft, cellTop, cellLeft + cell, cellTop + cell, SECTION_FILL);
            }
        }
        renderSectionBorders(graphics, left, top, inventory, cell);
    }

    private static void renderSectionBorders(GuiGraphics graphics, int left, int top, GridInventoryData inventory, int cell) {
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                String current = inventory.sectionAt(x, y);
                if (current == null) {
                    continue;
                }
                int cellLeft = left + GridLayoutMetrics.cellLeft(inventory, x, y, cell);
                int cellTop = top + GridLayoutMetrics.cellTop(inventory, x, y, cell);
                int renderLeft = cellLeft;
                int renderTop = cellTop;
                int renderRight = cellLeft + cell;
                int renderBottom = cellTop + cell;
                if (!hasSameSectionTouching(inventory, current, x, y, cell, Direction.LEFT)) {
                    graphics.fill(renderLeft, renderTop, renderLeft + SECTION_BORDER_WIDTH, renderBottom, SECTION_BORDER);
                }
                if (!hasSameSectionTouching(inventory, current, x, y, cell, Direction.RIGHT)) {
                    graphics.fill(renderRight - SECTION_BORDER_WIDTH, renderTop, renderRight, renderBottom, SECTION_BORDER);
                } else {
                    graphics.fill(renderRight - 1, renderTop, renderRight, renderBottom, SECTION_GRID_LINE);
                }
                if (!hasSameSectionTouching(inventory, current, x, y, cell, Direction.UP)) {
                    graphics.fill(renderLeft, renderTop, renderRight, renderTop + SECTION_BORDER_WIDTH, SECTION_BORDER);
                }
                if (!hasSameSectionTouching(inventory, current, x, y, cell, Direction.DOWN)) {
                    graphics.fill(renderLeft, renderBottom - SECTION_BORDER_WIDTH, renderRight, renderBottom, SECTION_BORDER);
                } else {
                    graphics.fill(renderLeft, renderBottom - 1, renderRight, renderBottom, SECTION_GRID_LINE);
                }
            }
        }
    }

    private static boolean hasSameSectionTouching(GridInventoryData inventory, String section, int x, int y, int cell, Direction direction) {
        int left = GridLayoutMetrics.cellLeft(inventory, x, y, cell);
        int top = GridLayoutMetrics.cellTop(inventory, x, y, cell);
        int right = left + cell;
        int bottom = top + cell;
        for (int otherY = 0; otherY < inventory.getRows(); otherY++) {
            for (int otherX = 0; otherX < inventory.getColumns(); otherX++) {
                if ((otherX == x && otherY == y) || !section.equals(inventory.sectionAt(otherX, otherY))) {
                    continue;
                }
                int otherLeft = GridLayoutMetrics.cellLeft(inventory, otherX, otherY, cell);
                int otherTop = GridLayoutMetrics.cellTop(inventory, otherX, otherY, cell);
                int otherRight = otherLeft + cell;
                int otherBottom = otherTop + cell;
                if (direction == Direction.LEFT && otherRight == left && rangesOverlap(top, bottom, otherTop, otherBottom)) {
                    return true;
                }
                if (direction == Direction.RIGHT && otherLeft == right && rangesOverlap(top, bottom, otherTop, otherBottom)) {
                    return true;
                }
                if (direction == Direction.UP && otherBottom == top && rangesOverlap(left, right, otherLeft, otherRight)) {
                    return true;
                }
                if (direction == Direction.DOWN && otherTop == bottom && rangesOverlap(left, right, otherLeft, otherRight)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean rangesOverlap(int aStart, int aEnd, int bStart, int bEnd) {
        return aStart < bEnd && aEnd > bStart;
    }

    private enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
