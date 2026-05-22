package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import net.minecraft.client.gui.GuiGraphics;

public final class GridItemRenderer {
    private GridItemRenderer() {
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, int gridLeft, int gridTop, int cell) {
        renderEntry(graphics, entry, gridLeft, gridTop, cell, 1.0F);
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, int gridLeft, int gridTop, int cell, float alpha) {
        int x = gridLeft + entry.x() * cell;
        int y = gridTop + entry.y() * cell;
        int bodyAlpha = Math.round(0xAA * alpha);
        int accentAlpha = Math.round(0xFF * alpha);
        graphics.fill(x + 1, y + 1, x + entry.width() * cell, y + entry.height() * cell, (bodyAlpha << 24) | 0x2D2D2D);
        graphics.fill(x + 1, y + 1, x + entry.width() * cell, y + 2, (accentAlpha << 24) | 0xB8A15B);
        renderStack(graphics, entry.stack(), x + 4, y + 4, alpha);
    }

    public static void renderStack(GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, int x, int y, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(net.minecraft.client.Minecraft.getInstance().font, stack, x, y);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
