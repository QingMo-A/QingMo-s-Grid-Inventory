package com.dreamingfish.gridinventory.client.ui.animation;

public final class HoverAnimation {
    private static final long DEFAULT_ENTER_DURATION_NANOS = 90_000_000L;
    private static final long DEFAULT_EXIT_DURATION_NANOS = 140_000_000L;

    private final long enterDurationNanos;
    private final long exitDurationNanos;

    private float startValue;
    private float targetValue;
    private float currentValue;
    private long transitionStartNanos;
    private long transitionDurationNanos;
    private boolean initialized;

    public HoverAnimation() {
        this(DEFAULT_ENTER_DURATION_NANOS, DEFAULT_EXIT_DURATION_NANOS);
    }

    public HoverAnimation(long enterDurationNanos, long exitDurationNanos) {
        this.enterDurationNanos = enterDurationNanos;
        this.exitDurationNanos = exitDurationNanos;
    }

    public float update(boolean hovered) {
        long now = System.nanoTime();
        if (!initialized) {
            initialized = true;
            startValue = 0.0F;
            targetValue = 0.0F;
            currentValue = 0.0F;
            transitionStartNanos = now;
            transitionDurationNanos = enterDurationNanos;
        }

        float displayedValue = valueAt(now);
        float nextTarget = hovered ? 1.0F : 0.0F;
        if (Float.compare(nextTarget, targetValue) != 0) {
            startValue = displayedValue;
            targetValue = nextTarget;
            transitionStartNanos = now;
            transitionDurationNanos = hovered ? enterDurationNanos : exitDurationNanos;
        }

        currentValue = valueAt(now);
        return currentValue;
    }

    public void reset() {
        startValue = 0.0F;
        targetValue = 0.0F;
        currentValue = 0.0F;
        transitionStartNanos = 0L;
        transitionDurationNanos = enterDurationNanos;
        initialized = false;
    }

    private float valueAt(long now) {
        if (transitionDurationNanos <= 0L) {
            return clamp(targetValue);
        }
        float elapsed = (float) (now - transitionStartNanos) / (float) transitionDurationNanos;
        if (elapsed >= 1.0F) {
            return clamp(targetValue);
        }
        if (elapsed <= 0.0F) {
            return clamp(startValue);
        }
        float eased = easeOutCubic(elapsed);
        return clamp(startValue + (targetValue - startValue) * eased);
    }

    private static float easeOutCubic(float value) {
        float inverse = 1.0F - clamp(value);
        return 1.0F - inverse * inverse * inverse;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
