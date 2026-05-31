package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.client.gui.GuiGraphics;

public final class GridRenderer {
    private static final int SECTION_GAP = 3;
    private static final int SECTION_BORDER_WIDTH = 1;
    private static final int SECTION_FILL = 0xFF111111;
    private static final int SECTION_BORDER = 0xFF555555;
    private static final int SECTION_GRID_LINE = 0xFF343434;

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
                String leftSection = inventory.sectionAt(x - 1, y);
                String rightSection = inventory.sectionAt(x + 1, y);
                String topSection = inventory.sectionAt(x, y - 1);
                String bottomSection = inventory.sectionAt(x, y + 1);
                if (leftSection == null) {
                    graphics.fill(renderLeft, renderTop, renderLeft + SECTION_BORDER_WIDTH, renderBottom, SECTION_BORDER);
                } else if (!sameSection(current, leftSection)) {
                    graphics.fill(renderLeft, renderTop, renderLeft + SECTION_BORDER_WIDTH, renderBottom, SECTION_BORDER);
                } else if (sameSection(current, leftSection)) {
                    graphics.fill(renderLeft, renderTop, renderLeft + 1, renderBottom, SECTION_GRID_LINE);
                }
                if (rightSection == null) {
                    graphics.fill(renderRight - SECTION_BORDER_WIDTH, renderTop, renderRight, renderBottom, SECTION_BORDER);
                } else if (!sameSection(current, rightSection)) {
                    graphics.fill(renderRight - SECTION_BORDER_WIDTH, renderTop, renderRight, renderBottom, SECTION_BORDER);
                } else if (sameSection(current, rightSection)) {
                    graphics.fill(renderRight - 1, renderTop, renderRight, renderBottom, SECTION_GRID_LINE);
                }
                if (topSection == null) {
                    graphics.fill(renderLeft, renderTop, renderRight, renderTop + SECTION_BORDER_WIDTH, SECTION_BORDER);
                } else if (!sameSection(current, topSection)) {
                    graphics.fill(renderLeft, renderTop, renderRight, renderTop + SECTION_BORDER_WIDTH, SECTION_BORDER);
                } else if (sameSection(current, topSection)) {
                    graphics.fill(renderLeft, renderTop, renderRight, renderTop + 1, SECTION_GRID_LINE);
                }
                if (bottomSection == null) {
                    graphics.fill(renderLeft, renderBottom - SECTION_BORDER_WIDTH, renderRight, renderBottom, SECTION_BORDER);
                } else if (!sameSection(current, bottomSection)) {
                    graphics.fill(renderLeft, renderBottom - SECTION_BORDER_WIDTH, renderRight, renderBottom, SECTION_BORDER);
                } else if (sameSection(current, bottomSection)) {
                    graphics.fill(renderLeft, renderBottom - 1, renderRight, renderBottom, SECTION_GRID_LINE);
                }
            }
        }
    }

    private static boolean sameSection(String first, String second) {
        return first != null && first.equals(second);
    }
}
