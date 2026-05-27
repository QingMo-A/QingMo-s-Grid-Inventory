package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.client.gui.GuiGraphics;

public final class GridRenderer {
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
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                if (!inventory.isEnabledCell(x, y)) {
                    continue;
                }
                int cellLeft = left + x * cell;
                int cellTop = top + y * cell;
                graphics.fill(cellLeft, cellTop, cellLeft + cell, cellTop + cell, 0xFF111111);
                graphics.renderOutline(cellLeft, cellTop, cell, cell, 0xFF555555);
                String section = inventory.sectionAt(x, y);
                if (!section.equals(inventory.sectionAt(x - 1, y))) {
                    graphics.fill(cellLeft, cellTop, cellLeft + 2, cellTop + cell, 0xFF8A8A8A);
                }
                if (!section.equals(inventory.sectionAt(x + 1, y))) {
                    graphics.fill(cellLeft + cell - 2, cellTop, cellLeft + cell, cellTop + cell, 0xFF8A8A8A);
                }
                if (!section.equals(inventory.sectionAt(x, y - 1))) {
                    graphics.fill(cellLeft, cellTop, cellLeft + cell, cellTop + 2, 0xFF8A8A8A);
                }
                if (!section.equals(inventory.sectionAt(x, y + 1))) {
                    graphics.fill(cellLeft, cellTop + cell - 2, cellLeft + cell, cellTop + cell, 0xFF8A8A8A);
                }
            }
        }
    }
}
