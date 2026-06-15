package com.dreamingfish.gridinventory.client.ui;

import net.minecraft.client.gui.GuiGraphics;

public final class GridUiMotion {
    public static final float HOVER_ICON_SCALE = 1.035F;
    public static final float HOVER_LIFT = 1.0F;

    public static final int NORMAL_SLOT_BACKGROUND = 0xFF20242A;
    public static final int HOVER_SLOT_BACKGROUND = 0xFF282E37;
    public static final int NORMAL_SLOT_OUTLINE = 0x664F5967;
    public static final int HOVER_SLOT_OUTLINE = 0xB8AAB5C4;

    private GridUiMotion() {
    }

    public static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public static float lift(float hoverProgress) {
        return HOVER_LIFT * clamp(hoverProgress);
    }

    public static float iconScale(float hoverProgress) {
        return 1.0F + (HOVER_ICON_SCALE - 1.0F) * clamp(hoverProgress);
    }

    public static int lerpArgb(int from, int to, float progress) {
        float value = clamp(progress);
        int a = lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, value);
        int r = lerp((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, value);
        int g = lerp((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, value);
        int b = lerp(from & 0xFF, to & 0xFF, value);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void renderShadow(GuiGraphics graphics, int x, int y, int width, int height, float hoverProgress) {
        float progress = clamp(hoverProgress);
        if (progress <= 0.0F) {
            return;
        }
        int firstAlpha = Math.round(0x24 * progress);
        int secondAlpha = Math.round(0x14 * progress);
        graphics.fill(x + 1, y + 1, x + width + 1, y + height + 1, firstAlpha << 24);
        graphics.fill(x + 1, y + 2, x + width + 1, y + height + 2, secondAlpha << 24);
    }

    private static int lerp(int from, int to, float progress) {
        return Math.round(from + (to - from) * progress);
    }
}
