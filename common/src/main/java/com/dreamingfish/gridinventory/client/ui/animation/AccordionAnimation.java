package com.dreamingfish.gridinventory.client.ui.animation;

public final class AccordionAnimation {
    private final HoverAnimation expansion = new HoverAnimation(190_000_000L, 160_000_000L);

    public float update(boolean expanded) {
        return expansion.update(expanded);
    }
}
