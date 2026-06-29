package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class ContainerGridSidebarPanel {
    private static final int CELL = 27;

    private int left;
    private int top;
    private int width;
    private int height;
    private int gridLeft;
    private int gridTop;
    private GridInventoryData gridData;
    private Component title = Component.empty();

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        updateGridOrigin();
    }

    public void setContainer(GridInventoryData gridData, Component title) {
        this.gridData = gridData;
        this.title = title;
        updateGridOrigin();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, boolean suppressTooltip) {
        graphics.fill(left, top, left + width, top + height, 0x88252A34);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, title, left + 6, top + 6, 0xE6EDF5, false);
        if (gridData == null) {
            return;
        }
        GridRenderer.renderGrid(graphics, gridLeft, gridTop, gridData, CELL);
        for (GridEntry entry : gridData.getEntries()) {
            GridItemRenderer.renderEntry(graphics, entry, gridLeft, gridTop, CELL, 1.0F, 0.0F);
        }
    }

    public Optional<GridEntryHit> entryAt(int mouseX, int mouseY) {
        if (gridData == null || !inGrid(mouseX, mouseY)) {
            return Optional.empty();
        }
        int x = cellX(mouseX, mouseY);
        int y = cellY(mouseX, mouseY);
        return gridData.getEntries().stream()
                .filter(entry -> entry.contains(x, y))
                .findFirst()
                .map(entry -> new GridEntryHit(entry, drawX(entry.x(), entry.y()), drawY(entry.x(), entry.y())));
    }

    public Optional<GridPlacementHit> placementAt(int mouseX, int mouseY) {
        if (gridData == null || !inGrid(mouseX, mouseY)) {
            return Optional.empty();
        }
        return Optional.of(new GridPlacementHit(cellX(mouseX, mouseY), cellY(mouseX, mouseY), gridLeft, gridTop));
    }

    public boolean isContainerPagePoint(double mouseX, double mouseY) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    private boolean inGrid(int mouseX, int mouseY) {
        return mouseX >= gridLeft && mouseY >= gridTop
                && mouseX < gridLeft + GridLayoutMetrics.width(gridData, CELL)
                && mouseY < gridTop + GridLayoutMetrics.height(gridData, CELL)
                && cellX(mouseX, mouseY) >= 0 && cellY(mouseX, mouseY) >= 0;
    }

    private int cellX(int mouseX, int mouseY) {
        return GridLayoutMetrics.cellXAt(gridData, mouseX - gridLeft, mouseY - gridTop, CELL);
    }

    private int cellY(int mouseX, int mouseY) {
        return GridLayoutMetrics.cellYAt(gridData, mouseX - gridLeft, mouseY - gridTop, CELL);
    }

    private int drawX(int x, int y) {
        return gridLeft + GridLayoutMetrics.cellLeft(gridData, x, y, CELL);
    }

    private int drawY(int x, int y) {
        return gridTop + GridLayoutMetrics.cellTop(gridData, x, y, CELL);
    }

    private void updateGridOrigin() {
        if (gridData == null) {
            gridLeft = left + 6;
            gridTop = top + 22;
            return;
        }
        int gridWidth = GridLayoutMetrics.width(gridData, CELL);
        gridLeft = left + Math.max(6, (width - gridWidth) / 2);
        gridTop = top + 22;
    }

    public record GridEntryHit(GridEntry entry, int drawX, int drawY) {
    }

    public record GridPlacementHit(int cellX, int cellY, int gridLeft, int gridTop) {
    }
}
