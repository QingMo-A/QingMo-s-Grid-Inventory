package com.dreamingfish.gridinventory.client.interaction;

import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface GridInteractionSurface {
    DropHit resolveDrop(GridDragSession session, int mouseX, int mouseY);

    record DropHit(Kind kind, @Nullable GridItemTarget target, @Nullable GridMoveOptions options) {
        public static DropHit pass() {
            return new DropHit(Kind.PASS, null, null);
        }

        public static DropHit blocked() {
            return new DropHit(Kind.BLOCKED, null, null);
        }

        public static DropHit target(GridItemTarget target) {
            return new DropHit(Kind.TARGET, target, null);
        }

        public static DropHit target(GridItemTarget target, GridMoveOptions options) {
            return new DropHit(Kind.TARGET, target, options);
        }
    }

    enum Kind {
        PASS,
        BLOCKED,
        TARGET
    }
}
