package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class GridRenderer {
    private static final int SECTION_GAP = 3;
    private static final int SECTION_BORDER_WIDTH = 1;
    private static final int SECTION_FILL = 0xFF111111;
    private static final int SECTION_BORDER = 0xFF555555;
    private static final int SECTION_GRID_LINE = 0x332F2F2F;
    private static final Map<GridInventoryData, Map<Integer, SectionRenderGeometry>> SECTION_GEOMETRY_CACHE = new WeakHashMap<>();

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
        SectionRenderGeometry geometry = geometry(inventory, cell);
        graphics.fill(left, top, left + geometry.width(), top + geometry.height(), 0x00111111);
        for (Rect rect : geometry.fills()) {
            graphics.fill(left + rect.x(), top + rect.y(), left + rect.right(), top + rect.bottom(), rect.color());
        }
        for (Rect rect : geometry.borders()) {
            graphics.fill(left + rect.x(), top + rect.y(), left + rect.right(), top + rect.bottom(), rect.color());
        }
    }

    private static SectionRenderGeometry geometry(GridInventoryData inventory, int cell) {
        synchronized (SECTION_GEOMETRY_CACHE) {
            return SECTION_GEOMETRY_CACHE.computeIfAbsent(inventory, ignored -> new HashMap<>())
                    .computeIfAbsent(cell, ignored -> computeGeometry(inventory, cell));
        }
    }

    private static SectionRenderGeometry computeGeometry(GridInventoryData inventory, int cell) {
        List<CellGeometry> cells = new ArrayList<>();
        List<Rect> fills = new ArrayList<>();
        List<Rect> borders = new ArrayList<>();
        for (int y = 0; y < inventory.getRows(); y++) {
            for (int x = 0; x < inventory.getColumns(); x++) {
                String current = inventory.sectionAt(x, y);
                if (current == null) {
                    continue;
                }
                int cellLeft = GridLayoutMetrics.cellLeft(inventory, x, y, cell);
                int cellTop = GridLayoutMetrics.cellTop(inventory, x, y, cell);
                cells.add(new CellGeometry(current, cellLeft, cellTop, cellLeft + cell, cellTop + cell));
                fills.add(new Rect(cellLeft, cellTop, cell, cell, SECTION_FILL));
            }
        }
        for (CellGeometry current : cells) {
            if (!hasSameSectionTouching(cells, current, Direction.LEFT)) {
                borders.add(new Rect(current.left(), current.top(), SECTION_BORDER_WIDTH, current.height(), SECTION_BORDER));
            }
            if (!hasSameSectionTouching(cells, current, Direction.RIGHT)) {
                borders.add(new Rect(current.right() - SECTION_BORDER_WIDTH, current.top(), SECTION_BORDER_WIDTH, current.height(), SECTION_BORDER));
            } else {
                borders.add(new Rect(current.right() - 1, current.top(), 1, current.height(), SECTION_GRID_LINE));
            }
            if (!hasSameSectionTouching(cells, current, Direction.UP)) {
                borders.add(new Rect(current.left(), current.top(), current.width(), SECTION_BORDER_WIDTH, SECTION_BORDER));
            }
            if (!hasSameSectionTouching(cells, current, Direction.DOWN)) {
                borders.add(new Rect(current.left(), current.bottom() - SECTION_BORDER_WIDTH, current.width(), SECTION_BORDER_WIDTH, SECTION_BORDER));
            } else {
                borders.add(new Rect(current.left(), current.bottom() - 1, current.width(), 1, SECTION_GRID_LINE));
            }
        }
        return new SectionRenderGeometry(GridLayoutMetrics.width(inventory, cell), GridLayoutMetrics.height(inventory, cell),
                List.copyOf(fills), List.copyOf(borders));
    }

    private static boolean hasSameSectionTouching(List<CellGeometry> cells, CellGeometry current, Direction direction) {
        for (CellGeometry other : cells) {
            if (other == current || !current.section().equals(other.section())) {
                continue;
            }
            if (direction == Direction.LEFT && other.right() == current.left() && rangesOverlap(current.top(), current.bottom(), other.top(), other.bottom())) {
                return true;
            }
            if (direction == Direction.RIGHT && other.left() == current.right() && rangesOverlap(current.top(), current.bottom(), other.top(), other.bottom())) {
                return true;
            }
            if (direction == Direction.UP && other.bottom() == current.top() && rangesOverlap(current.left(), current.right(), other.left(), other.right())) {
                return true;
            }
            if (direction == Direction.DOWN && other.top() == current.bottom() && rangesOverlap(current.left(), current.right(), other.left(), other.right())) {
                return true;
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

    private record SectionRenderGeometry(int width, int height, List<Rect> fills, List<Rect> borders) {
    }

    private record CellGeometry(String section, int left, int top, int right, int bottom) {
        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }

    private record Rect(int x, int y, int width, int height, int color) {
        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }
    }
}
