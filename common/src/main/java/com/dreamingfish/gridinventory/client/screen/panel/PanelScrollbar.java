package com.dreamingfish.gridinventory.client.screen.panel;

import net.minecraft.client.gui.GuiGraphics;

final class PanelScrollbar {
    private int viewportLeft;
    private int viewportTop;
    private int viewportWidth;
    private int viewportHeight;
    private int contentHeight;
    private int targetScroll;
    private double scroll;
    private double velocity;
    private boolean dragging;
    private int dragOffset;
    private float alpha;

    void update(int viewportLeft, int viewportTop, int viewportWidth, int viewportHeight, int contentHeight) {
        this.viewportLeft = viewportLeft;
        this.viewportTop = viewportTop;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.contentHeight = Math.max(viewportHeight, contentHeight);
        targetScroll = clamp(targetScroll, 0, maxScroll());
        scroll = clamp(scroll, 0.0D, maxScroll());
        if ((scroll <= 0.0D && velocity < 0.0D) || (scroll >= maxScroll() && velocity > 0.0D)) {
            velocity = 0.0D;
        }
    }

    int scroll() {
        if (dragging) {
            scroll = targetScroll;
            velocity = 0.0D;
        } else if (Math.abs(velocity) > 0.05D) {
            scroll = clamp(scroll + velocity, 0.0D, maxScroll());
            targetScroll = (int) Math.round(scroll);
            velocity *= 0.78D;
            if ((scroll <= 0.0D && velocity < 0.0D) || (scroll >= maxScroll() && velocity > 0.0D)) {
                velocity = 0.0D;
            }
        } else if (Math.abs(scroll - targetScroll) < 0.35D) {
            scroll = targetScroll;
            velocity = 0.0D;
        } else {
            scroll += (targetScroll - scroll) * 0.22D;
        }
        return (int) Math.round(scroll);
    }

    boolean mouseScrolled(double mouseX, double mouseY, double deltaY, int amount) {
        if (!inViewport(mouseX, mouseY) || !canScroll()) {
            return false;
        }
        double impulse = -Math.signum(deltaY) * amount * 0.42D;
        velocity = clamp(velocity + impulse, -amount * 1.35D, amount * 1.35D);
        return true;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !canScroll()) {
            return false;
        }
        if (!inTrack(mouseX, mouseY)) {
            return false;
        }
        int thumbTop = thumbTop();
        int thumbBottom = thumbTop + thumbHeight();
        if (mouseY >= thumbTop && mouseY < thumbBottom) {
            dragging = true;
            dragOffset = (int) mouseY - thumbTop;
        } else {
            moveThumbTo((int) mouseY - thumbHeight() / 2);
            dragging = true;
            dragOffset = thumbHeight() / 2;
        }
        scroll = targetScroll;
        return true;
    }

    boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (button != 0 || !dragging) {
            return false;
        }
        moveThumbTo((int) mouseY - dragOffset);
        scroll = targetScroll;
        return true;
    }

    boolean mouseReleased(int button) {
        if (button != 0 || !dragging) {
            return false;
        }
        dragging = false;
        return true;
    }

    void render(GuiGraphics graphics, double mouseX, double mouseY) {
        if (!canScroll()) {
            alpha = approach(alpha, 0.0F, 0.08F);
            return;
        }
        boolean active = dragging || inViewport(mouseX, mouseY) || inTrack(mouseX, mouseY);
        alpha = approach(alpha, active ? 1.0F : 0.0F, active ? 0.18F : 0.06F);
        if (alpha <= 0.03F) {
            return;
        }
        int trackX = trackX();
        int trackTop = viewportTop + 2;
        int trackBottom = viewportTop + viewportHeight - 2;
        int trackAlpha = (int) (0x36 * alpha);
        int thumbAlpha = (int) ((dragging ? 0xF0 : 0xB8) * alpha);
        drawPill(graphics, trackX + 1, trackTop, 2, trackBottom - trackTop, argb(trackAlpha, 225, 228, 232));
        drawPill(graphics, trackX, thumbTop(), 4, thumbHeight(), argb(thumbAlpha, 210, 218, 226));
    }

    private void moveThumbTo(int thumbTop) {
        int travel = thumbTravel();
        if (travel <= 0) {
            targetScroll = 0;
            velocity = 0.0D;
            return;
        }
        int relative = clamp(thumbTop - (viewportTop + 2), 0, travel);
        targetScroll = relative * maxScroll() / travel;
        velocity = 0.0D;
    }

    private boolean canScroll() {
        return contentHeight > viewportHeight && viewportHeight > 0;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - viewportHeight);
    }

    private int trackX() {
        return viewportLeft + viewportWidth - 8;
    }

    private int thumbHeight() {
        if (!canScroll()) {
            return 0;
        }
        int trackHeight = Math.max(1, viewportHeight - 4);
        return Math.max(20, trackHeight * viewportHeight / contentHeight);
    }

    private int thumbTravel() {
        return Math.max(0, viewportHeight - 4 - thumbHeight());
    }

    private int thumbTop() {
        int max = maxScroll();
        if (max <= 0) {
            return viewportTop + 2;
        }
        return viewportTop + 2 + thumbTravel() * targetScroll / max;
    }

    private boolean inViewport(double mouseX, double mouseY) {
        return mouseX >= viewportLeft && mouseY >= viewportTop
                && mouseX < viewportLeft + viewportWidth
                && mouseY < viewportTop + viewportHeight;
    }

    private boolean inTrack(double mouseX, double mouseY) {
        return mouseX >= trackX() - 6 && mouseX < trackX() + 10
                && mouseY >= viewportTop && mouseY < viewportTop + viewportHeight;
    }

    private static void drawPill(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (height <= 0 || width <= 0) {
            return;
        }
        if (width <= 2) {
            graphics.fill(x, y + 1, x + width, y + height - 1, color);
            return;
        }
        graphics.fill(x + 1, y, x + width - 1, y + height, color);
        graphics.fill(x, y + 1, x + width, y + height - 1, color);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (clamp(alpha, 0, 255) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static float approach(float value, float target, float step) {
        if (value < target) {
            return Math.min(target, value + step);
        }
        return Math.max(target, value - step);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
