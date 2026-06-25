package com.dreamingfish.gridinventory.common.inventory;

public record GridMoveOptions(int count, boolean rotated, boolean targetFolded) {
    public static GridMoveOptions all(boolean rotated, boolean targetFolded) {
        return new GridMoveOptions(Integer.MAX_VALUE, rotated, targetFolded);
    }

    public int safeCount() {
        return count <= 0 ? Integer.MAX_VALUE : count;
    }
}
