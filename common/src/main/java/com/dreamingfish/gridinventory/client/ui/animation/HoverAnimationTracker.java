package com.dreamingfish.gridinventory.client.ui.animation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class HoverAnimationTracker<K> {
    private static final long CLEANUP_INTERVAL_NANOS = 1_000_000_000L;
    private static final long STALE_ANIMATION_NANOS = 2_000_000_000L;

    private final Map<K, TrackedAnimation> animations = new HashMap<>();
    private long lastCleanupNanos;

    public float update(K key, boolean hovered) {
        Objects.requireNonNull(key, "key");
        long now = System.nanoTime();
        TrackedAnimation tracked = animations.computeIfAbsent(key, ignored -> new TrackedAnimation());
        tracked.lastSeenNanos = now;
        return tracked.animation.update(hovered);
    }

    public void markFrameEnd() {
        long now = System.nanoTime();
        if (now - lastCleanupNanos < CLEANUP_INTERVAL_NANOS) {
            return;
        }
        lastCleanupNanos = now;
        Iterator<Map.Entry<K, TrackedAnimation>> iterator = animations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, TrackedAnimation> entry = iterator.next();
            if (now - entry.getValue().lastSeenNanos > STALE_ANIMATION_NANOS) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        animations.clear();
    }

    private static final class TrackedAnimation {
        private final HoverAnimation animation = new HoverAnimation();
        private long lastSeenNanos;
    }
}
