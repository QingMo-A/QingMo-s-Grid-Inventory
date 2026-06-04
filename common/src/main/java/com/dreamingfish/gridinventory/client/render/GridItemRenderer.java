package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

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
        renderStackInArea(graphics, entry.stack(), x, y, entry.width() * cell, entry.height() * cell, alpha, entry.rotated());
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, GridInventoryData inventory, int gridLeft, int gridTop, int cell, float alpha) {
        int x = gridLeft + GridLayoutMetrics.cellLeft(inventory, entry.x(), entry.y(), cell);
        int y = gridTop + GridLayoutMetrics.cellTop(inventory, entry.x(), entry.y(), cell);
        int width = GridLayoutMetrics.areaWidth(inventory, entry.x(), entry.y(), entry.width(), cell);
        int height = GridLayoutMetrics.areaHeight(inventory, entry.x(), entry.y(), entry.height(), cell);
        int bodyAlpha = Math.round(0xAA * alpha);
        int accentAlpha = Math.round(0xFF * alpha);
        graphics.fill(x + 1, y + 1, x + width, y + height, (bodyAlpha << 24) | 0x2D2D2D);
        graphics.fill(x + 1, y + 1, x + width, y + 2, (accentAlpha << 24) | 0xB8A15B);
        renderStackInArea(graphics, entry.stack(), x, y, width, height, alpha, entry.rotated());
    }

    public static void renderStack(GuiGraphics graphics, ItemStack stack, int x, int y, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha) {
        renderStackInArea(graphics, stack, x, y, width, height, alpha, false);
    }

    public static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha, boolean rotated) {
        int padding = GridInventoryServices.clientConfig().gridItemInnerPadding();
        int iconSize = Math.max(8, Math.min(width, height) - padding * 2);
        float scale = iconSize / 16.0F;
        // Grid lines consume the top and left pixels, so center icons in the visible inner area.
        float iconCenterX = x + width / 2.0F + 0.5F;
        float iconCenterY = y + height / 2.0F + 0.5F;

        graphics.pose().pushPose();
        graphics.pose().translate(iconCenterX, iconCenterY, 0.0F);
        if (rotated) {
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(-90.0F));
        }
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItem(stack, -8, -8);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();

        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + width - 17, y + height - 17);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
