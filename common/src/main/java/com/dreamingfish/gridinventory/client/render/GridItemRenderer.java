package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.client.compat.rarity.GridItemRarityServices;
import com.dreamingfish.gridinventory.client.ui.GridUiMotion;
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
        renderEntry(graphics, entry, gridLeft, gridTop, cell, alpha, 0.0F);
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, int gridLeft, int gridTop, int cell, float alpha, float hoverProgress) {
        int x = gridLeft + entry.x() * cell;
        int y = gridTop + entry.y() * cell;
        renderEntryArea(graphics, entry.stack(), x, y, entry.width() * cell, entry.height() * cell,
                1, 1, 1, 1, alpha, entry.rotated(), hoverProgress);
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, GridInventoryData inventory, int gridLeft, int gridTop, int cell, float alpha) {
        renderEntry(graphics, entry, inventory, gridLeft, gridTop, cell, alpha, 0.0F);
    }

    public static void renderEntry(GuiGraphics graphics, GridEntry entry, GridInventoryData inventory, int gridLeft, int gridTop, int cell, float alpha, float hoverProgress) {
        int x = gridLeft + GridLayoutMetrics.cellLeft(inventory, entry.x(), entry.y(), cell);
        int y = gridTop + GridLayoutMetrics.cellTop(inventory, entry.x(), entry.y(), cell);
        int width = GridLayoutMetrics.areaWidth(inventory, entry.x(), entry.y(), entry.width(), cell);
        int height = GridLayoutMetrics.areaHeight(inventory, entry.x(), entry.y(), entry.height(), cell);
        int leftInset = needsLeftInset(inventory, entry) ? 1 : 0;
        int topInset = needsTopInset(inventory, entry) ? 1 : 0;
        renderEntryArea(graphics, entry.stack(), x, y, width, height, leftInset, topInset, 1, 1, alpha, entry.rotated(), hoverProgress);
    }

    private static void renderEntryArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height,
                                        int leftInset, int topInset, int rightInset, int bottomInset,
                                        float alpha, boolean rotated, float hoverProgress) {
        float visualHover = alpha >= 1.0F ? GridUiMotion.clamp(hoverProgress) : 0.0F;
        GridUiMotion.renderShadow(graphics, x + leftInset, y + topInset,
                width - leftInset - rightInset, height - topInset - bottomInset, visualHover);
        int bodyAlpha = Math.round(0xAA * alpha);
        boolean rarityBackground = GridItemRarityServices.compat().renderBackground(graphics, stack,
                x + leftInset, y + topInset,
                width - leftInset - rightInset, height - topInset - bottomInset, alpha);
        if (!rarityBackground) {
            int normal = (bodyAlpha << 24) | 0x2D2D2D;
            int hover = (bodyAlpha << 24) | 0x3A4048;
            graphics.fill(x + leftInset, y + topInset, x + width - rightInset, y + height - bottomInset,
                    GridUiMotion.lerpArgb(normal, hover, visualHover));
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -GridUiMotion.lift(visualHover), visualHover > 0.0F ? 8.0F : 0.0F);
        renderStackInArea(graphics, stack, x + leftInset, y + topInset,
                width - leftInset - rightInset, height - topInset - bottomInset, alpha, rotated, false, visualHover);
        graphics.pose().popPose();
        if (visualHover > 0.0F) {
            graphics.renderOutline(x + leftInset, y + topInset,
                    width - leftInset - rightInset, height - topInset - bottomInset,
                    GridUiMotion.lerpArgb(0x004F5967, GridUiMotion.HOVER_SLOT_OUTLINE, visualHover));
        }
    }

    private static boolean needsLeftInset(GridInventoryData inventory, GridEntry entry) {
        if (!inventory.hasCustomSections()) {
            return true;
        }
        String section = inventory.sectionAt(entry.x(), entry.y());
        return section == null || entry.x() <= 0 || !section.equals(inventory.sectionAt(entry.x() - 1, entry.y()));
    }

    private static boolean needsTopInset(GridInventoryData inventory, GridEntry entry) {
        if (!inventory.hasCustomSections()) {
            return true;
        }
        String section = inventory.sectionAt(entry.x(), entry.y());
        return section == null || entry.y() <= 0 || !section.equals(inventory.sectionAt(entry.x(), entry.y() - 1));
    }

    public static void renderStack(GuiGraphics graphics, ItemStack stack, int x, int y, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItem(stack, x, y);
        GridItemRarityServices.compat().renderItemDecorations(graphics, Minecraft.getInstance().font, stack, x, y);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha) {
        renderStackInArea(graphics, stack, x, y, width, height, alpha, false, 0.0F);
    }

    public static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha, boolean rotated) {
        renderStackInArea(graphics, stack, x, y, width, height, alpha, rotated, true, 0.0F);
    }

    public static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha, float hoverProgress) {
        renderStackInArea(graphics, stack, x, y, width, height, alpha, false, true, hoverProgress);
    }

    public static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha, boolean rotated, float hoverProgress) {
        renderStackInArea(graphics, stack, x, y, width, height, alpha, rotated, true, hoverProgress);
    }

    private static void renderStackInArea(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha, boolean rotated, boolean renderRarityBackground, float hoverProgress) {
        int padding = GridInventoryServices.clientConfig().gridItemInnerPadding();
        int iconSize = Math.max(8, Math.min(width, height) - padding * 2);
        float visualHover = alpha >= 1.0F ? GridUiMotion.clamp(hoverProgress) : 0.0F;
        float scale = iconSize / 16.0F * GridUiMotion.iconScale(visualHover);
        // Grid lines consume the top and left pixels, so center icons in the visible inner area.
        float iconCenterX = x + width / 2.0F + 0.5F;
        float iconCenterY = y + height / 2.0F + 0.5F;

        if (renderRarityBackground) {
            GridItemRarityServices.compat().renderBackground(graphics, stack, x + 1, y + 1,
                    Math.max(0, width - 2), Math.max(0, height - 2), alpha);
        }

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
        GridItemRarityServices.compat().renderItemDecorations(graphics, Minecraft.getInstance().font, stack, x + width - 17, y + height - 17);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
