package com.dreamingfish.gridinventory.client.ui.animation;

public final class AnimatedButtonState {
    private final HoverAnimation hover = new HoverAnimation();
    private final HoverAnimation press = new HoverAnimation(60_000_000L, 80_000_000L);
    private final HoverAnimation active = new HoverAnimation(160_000_000L, 140_000_000L);

    public ButtonFrame update(boolean hovered, boolean pressed, boolean active) {
        return new ButtonFrame(this.hover.update(hovered), this.press.update(pressed), this.active.update(active));
    }

    public record ButtonFrame(float hoverProgress, float pressProgress, float activeProgress) {
    }
}
