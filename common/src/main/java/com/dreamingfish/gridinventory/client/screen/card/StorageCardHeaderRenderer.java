package com.dreamingfish.gridinventory.client.screen.card;

import com.dreamingfish.gridinventory.client.ui.GridUiMotion;
import com.dreamingfish.gridinventory.client.ui.animation.AnimatedButtonState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class StorageCardHeaderRenderer {
    public static final int HEIGHT = 46;

    private StorageCardHeaderRenderer() {
    }

    public static void render(GuiGraphics graphics, StorageCardData data, int x, int y, int width,
                              AnimatedButtonState.ButtonFrame frame) {
        float hover = frame.hoverProgress();
        float active = frame.activeProgress();
        float pressed = frame.pressProgress();
        int lift = Math.round(GridUiMotion.HOVER_LIFT * hover) - Math.round(pressed);
        GridUiMotion.renderShadow(graphics, x, y, width, HEIGHT, Math.max(hover, active * 0.65F));
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -lift, 6.0F);
        int bg = GridUiMotion.lerpArgb(0x661A1F27, 0x8A222A35, hover);
        bg = GridUiMotion.lerpArgb(bg, 0x99202733, active);
        bg = GridUiMotion.lerpArgb(bg, 0x70171C23, pressed);
        fillSoftPanel(graphics, x, y, width, HEIGHT, bg);
        int border = GridUiMotion.lerpArgb(0x22FFFFFF, 0x55AABEDC, Math.max(hover, active));
        graphics.renderOutline(x, y, width, HEIGHT, border);
        if (pressed > 0.0F) {
            graphics.fill(x + 2, y + 2, x + width - 2, y + HEIGHT - 2, (Math.round(0x22 * pressed) << 24));
        }
        if (active > 0.0F) {
            graphics.fill(x, y + HEIGHT - 2, x + Math.round(width * active), y + HEIGHT, 0xAA7AA2F7);
        }

        Font font = Minecraft.getInstance().font;
        int iconX = x + 10;
        int iconY = y + (HEIGHT - 16) / 2;
        if (!data.icon().isEmpty()) {
            float iconScale = 1.0F + 0.025F * hover;
            graphics.pose().pushPose();
            graphics.pose().translate(iconX + 8, iconY + 8, 0.0F);
            graphics.pose().scale(iconScale, iconScale, 1.0F);
            graphics.renderItem(data.icon(), -8, -8);
            graphics.pose().popPose();
        } else {
            graphics.fill(iconX, iconY, iconX + 16, iconY + 16, data.available() ? 0x446A7280 : 0x33535A66);
            graphics.renderOutline(iconX, iconY, 16, 16, 0x447A8594);
        }

        String capacity = data.totalCells() > 0 ? data.usedCells() + " / " + data.totalCells() : "";
        int rightReserve = 18 + (capacity.isEmpty() ? 0 : font.width(capacity) + 10);
        int titleX = x + 34;
        int titleWidth = Math.max(20, width - 44 - rightReserve);
        graphics.drawString(font, font.plainSubstrByWidth(data.title().getString(), titleWidth),
                titleX, y + 9, data.available() ? 0xF1F5F9 : 0x8792A0, false);
        graphics.drawString(font, font.plainSubstrByWidth(data.subtitle().getString(), titleWidth),
                titleX, y + 25, 0x94A3B8, false);
        if (!capacity.isEmpty()) {
            graphics.drawString(font, capacity, x + width - rightReserve, y + (HEIGHT - font.lineHeight) / 2, 0xB8C4D3, false);
        }
        drawChevron(graphics, x + width - 14, y + 22, active);
        graphics.pose().popPose();
    }

    private static void fillSoftPanel(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 2, y, x + width - 2, y + height, color);
        graphics.fill(x, y + 2, x + width, y + height - 2, color);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
    }

    private static void drawChevron(GuiGraphics graphics, int centerX, int centerY, float active) {
        int spread = 4;
        int offset = Math.round(2.0F * active);
        int color = 0xFFD8E2F0;
        if (active < 0.5F) {
            graphics.fill(centerX - spread, centerY - 1, centerX, centerY + 1, color);
            graphics.fill(centerX, centerY + offset, centerX + spread, centerY + offset + 2, color);
        } else {
            graphics.fill(centerX - spread, centerY + offset, centerX, centerY + offset + 2, color);
            graphics.fill(centerX, centerY - 1, centerX + spread, centerY + 1, color);
        }
    }
}
