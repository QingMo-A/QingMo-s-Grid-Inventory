package com.dreamingfish.gridinventory.client.interaction;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Mutable client-side state for one grid item drag. Screens keep visual source hits separately;
 * placement-affecting state lives here so every grid surface uses the same rules.
 */
public final class GridDragSession {
    private final int cellSize;
    private GridItemSource source;
    private ItemStack previewStack = ItemStack.EMPTY;
    private boolean rotated;
    private boolean folded;
    private int requestedCount = Integer.MAX_VALUE;
    private int anchorCellX;
    private int anchorCellY;
    private int anchorPixelX;
    private int anchorPixelY;

    public GridDragSession(int cellSize) {
        this.cellSize = cellSize;
        resetAnchor();
    }

    public void begin(@Nullable GridItemSource source, ItemStack stack, boolean rotated) {
        begin(source, stack, rotated, Integer.MAX_VALUE);
    }

    public void begin(@Nullable GridItemSource source, ItemStack stack, boolean rotated, int requestedCount) {
        this.source = source;
        this.previewStack = stack.copy();
        this.rotated = rotated;
        this.folded = GridBackpackItem.isFolded(previewStack);
        this.requestedCount = requestedCount <= 0 ? Integer.MAX_VALUE : requestedCount;
        resetAnchor();
    }

    public boolean isActive() {
        return !previewStack.isEmpty();
    }

    public Optional<GridItemSource> source() {
        return Optional.ofNullable(source);
    }

    public ItemStack stack() {
        return previewStack;
    }

    public boolean rotated() {
        return rotated;
    }

    public boolean folded() {
        return folded;
    }

    public int requestedCount() {
        return requestedCount;
    }

    public int anchorCellX() {
        if (previewStack.isEmpty()) {
            return 0;
        }
        int width = GridItemSizeManager.getSize(previewStack).placedWidth(rotated);
        return Math.max(0, Math.min(anchorCellX, width - 1));
    }

    public int anchorCellY() {
        if (previewStack.isEmpty()) {
            return 0;
        }
        int height = GridItemSizeManager.getSize(previewStack).placedHeight(rotated);
        return Math.max(0, Math.min(anchorCellY, height - 1));
    }

    public int anchorPixelX() {
        return anchorPixelX;
    }

    public int anchorPixelY() {
        return anchorPixelY;
    }

    public void setGridAnchor(int mouseX, int mouseY, int itemLeft, int itemTop, int width, int height) {
        int relativeX = Math.max(0, Math.min(width * cellSize - 1, mouseX - itemLeft));
        int relativeY = Math.max(0, Math.min(height * cellSize - 1, mouseY - itemTop));
        anchorCellX = relativeX / cellSize;
        anchorCellY = relativeY / cellSize;
        anchorPixelX = relativeX % cellSize;
        anchorPixelY = relativeY % cellSize;
    }

    public void setPixelAnchor(int pixelX, int pixelY) {
        anchorCellX = 0;
        anchorCellY = 0;
        anchorPixelX = Math.max(0, Math.min(cellSize - 1, pixelX));
        anchorPixelY = Math.max(0, Math.min(cellSize - 1, pixelY));
    }

    public void setCellAnchor(int cellX, int cellY) {
        anchorCellX = Math.max(0, cellX);
        anchorCellY = Math.max(0, cellY);
        anchorPixelX = cellSize / 2;
        anchorPixelY = cellSize / 2;
    }

    public boolean rotate() {
        if (previewStack.isEmpty()) {
            return false;
        }
        GridItemSize size = GridItemSizeManager.getSize(previewStack);
        if (!size.rotatable() || size.width() == size.height()) {
            return false;
        }
        rotated = !rotated;
        return true;
    }

    public boolean toggleFolded() {
        if (!(previewStack.getItem() instanceof GridBackpackItem)) {
            return false;
        }
        ItemStack toggled = previewStack.copy();
        if (!GridBackpackItem.toggleFolded(toggled)) {
            return false;
        }
        previewStack = toggled;
        folded = GridBackpackItem.isFolded(toggled);
        if (!GridItemSizeManager.getSize(toggled).rotatable()) {
            rotated = false;
        }
        return true;
    }

    public GridMoveOptions options() {
        return new GridMoveOptions(requestedCount, rotated, folded);
    }

    public GridMoveOptions options(int count, boolean targetFolded) {
        return new GridMoveOptions(count, rotated, targetFolded);
    }

    public void clear() {
        source = null;
        previewStack = ItemStack.EMPTY;
        rotated = false;
        folded = false;
        requestedCount = Integer.MAX_VALUE;
        resetAnchor();
    }

    private void resetAnchor() {
        anchorCellX = 0;
        anchorCellY = 0;
        anchorPixelX = cellSize / 2;
        anchorPixelY = cellSize / 2;
    }
}
