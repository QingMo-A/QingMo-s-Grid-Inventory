package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import net.minecraft.client.gui.GuiGraphics;

public final class GridItemRenderer {
    private GridItemRenderer() {
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, int gridLeft, int gridTop, int cell) {
        int x = gridLeft + entry.x() * cell;
        int y = gridTop + entry.y() * cell;
        graphics.fill(x + 1, y + 1, x + entry.width() * cell, y + entry.height() * cell, 0xAA2D2D2D);
        graphics.fill(x + 1, y + 1, x + entry.width() * cell, y + 2, 0xFFB8A15B);
        graphics.renderItem(entry.stack(), x + 4, y + 4);
        graphics.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, entry.stack(), x + 4, y + 4);
    }
}
