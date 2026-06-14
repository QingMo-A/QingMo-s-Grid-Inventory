package com.dreamingfish.gridinventory.client.compat.rarity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class GridItemRarityCompat {
    public static final GridItemRarityCompat NOOP = new GridItemRarityCompat() {
        @Override
        protected @Nullable RarityVisual queryVisual(ItemStack stack) {
            return null;
        }

        @Override
        protected void setAutomaticBorderSuppressed(boolean suppressed) {
        }
    };

    private final ThreadLocal<Integer> suppressionDepth = ThreadLocal.withInitial(() -> 0);

    public record RarityVisual(int rarity, int rgb) {
    }

    protected abstract @Nullable RarityVisual queryVisual(ItemStack stack);

    protected abstract void setAutomaticBorderSuppressed(boolean suppressed);

    public final boolean hasVisual(ItemStack stack) {
        return !stack.isEmpty() && queryVisual(stack) != null;
    }

    public final boolean renderBackground(GuiGraphics graphics, ItemStack stack, int x, int y, int width, int height, float alpha) {
        if (stack.isEmpty() || width <= 0 || height <= 0 || alpha < 1.0F) {
            return false;
        }
        RarityVisual visual = queryVisual(stack);
        if (visual == null) {
            return false;
        }
        int rgb = softenColor(visual.rgb() & 0xFFFFFF);
        int fillAlpha = Math.max(0, Math.min(255, Math.round(0x48 * alpha)));
        graphics.fill(x, y, x + width, y + height, (fillAlpha << 24) | rgb);
        return true;
    }

    private static int softenColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int gray = Math.round(red * 0.299F + green * 0.587F + blue * 0.114F);
        red = softenChannel(red, gray);
        green = softenChannel(green, gray);
        blue = softenChannel(blue, gray);
        return (red << 16) | (green << 8) | blue;
    }

    private static int softenChannel(int channel, int gray) {
        int desaturated = Math.round(gray + (channel - gray) * 0.62F);
        return Math.max(0, Math.min(255, Math.round(desaturated * 0.78F)));
    }

    public final void renderItemDecorations(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        int previous = suppressionDepth.get();
        suppressionDepth.set(previous + 1);
        if (previous == 0) {
            setAutomaticBorderSuppressed(true);
        }
        try {
            graphics.renderItemDecorations(font, stack, x, y);
        } finally {
            int remaining = suppressionDepth.get() - 1;
            if (remaining <= 0) {
                suppressionDepth.remove();
                setAutomaticBorderSuppressed(false);
            } else {
                suppressionDepth.set(remaining);
            }
        }
    }
}
