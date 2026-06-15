package com.dreamingfish.gridinventory.client.screen.card;

import com.dreamingfish.gridinventory.client.ui.animation.AccordionAnimation;
import com.dreamingfish.gridinventory.client.ui.animation.AnimatedButtonState;

public final class StorageAccordionState {
    private final AnimatedButtonState button = new AnimatedButtonState();
    private final AccordionAnimation accordion = new AccordionAnimation();
    private boolean expanded = true;
    private boolean pressed;
    private float expansionProgress = 1.0F;
    private AnimatedButtonState.ButtonFrame buttonFrame = new AnimatedButtonState.ButtonFrame(0.0F, 0.0F, 1.0F);

    public void update(boolean hovered, boolean available) {
        if (!available) {
            expanded = false;
        }
        buttonFrame = button.update(hovered, pressed, expanded && available);
        expansionProgress = accordion.update(expanded && available);
        pressed = false;
    }

    public void toggle(boolean available) {
        if (available) {
            expanded = !expanded;
            pressed = true;
        }
    }

    public float expansionProgress() {
        return expansionProgress;
    }

    public AnimatedButtonState.ButtonFrame buttonFrame() {
        return buttonFrame;
    }

    public boolean expanded() {
        return expanded;
    }
}
