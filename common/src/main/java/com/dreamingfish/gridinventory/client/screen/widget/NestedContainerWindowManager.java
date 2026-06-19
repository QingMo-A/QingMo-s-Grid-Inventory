package com.dreamingfish.gridinventory.client.screen.widget;

import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.client.ui.animation.HoverAnimationTracker;
import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerAccess;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class NestedContainerWindowManager {
    private static final long REFRESH_INTERVAL_NANOS = 150_000_000L;
    private static final int CELL = 27;
    private static final int GRID_BORDER = 1;
    private static final int PADDING = 8;
    private static final int HEADER_HEIGHT = 22;
    private static final int CLOSE_SIZE = 12;
    private static final int CONTAINER_GAP = 10;
    private static final int TITLE_HEIGHT = 12;
    private static final float WINDOW_BASE_Z = 420.0F;
    private static final float WINDOW_Z_STEP = 10.0F;
    private final List<Window> windows = new ArrayList<>();
    private final HoverAnimationTracker<EntryAnimationKey> entryHoverAnimations = new HoverAnimationTracker<>();
    private Window dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private long lastRefreshNanos;

    public boolean open(ItemStack stack, NestedContainerPath path, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (stack.isEmpty() || !NestedContainerAccess.hasOpenableContainer(stack)) {
            return false;
        }
        for (Window window : windows) {
            if (window.path.equals(path)) {
                bringToFront(window);
                return true;
            }
        }
        Window window = Window.create(stack.copy(), path, mouseX + 12, mouseY + 10);
        window.clamp(screenWidth, screenHeight);
        windows.add(window);
        bringToFront(window);
        return true;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, boolean hoverEnabled, Optional<PlacementPreview> preview) {
        boolean windowDragging = dragging != null;
        for (int index = 0; index < windows.size(); index++) {
            Window window = windows.get(index);
            window.render(graphics, mouseX, mouseY, hoverEnabled && !windowDragging, preview, entryHoverAnimations,
                    WINDOW_BASE_Z + index * WINDOW_Z_STEP);
        }
        entryHoverAnimations.markFrameEnd();
    }

    public void refresh(Function<NestedContainerPath, Optional<ItemStack>> resolver) {
        long now = System.nanoTime();
        if (now - lastRefreshNanos < REFRESH_INTERVAL_NANOS) {
            return;
        }
        lastRefreshNanos = now;
        windows.removeIf(window -> !window.refresh(resolver));
    }

    public Optional<GridHit> gridAt(int mouseX, int mouseY) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Optional<GridHit> hit = windows.get(i).gridAt(mouseX, mouseY);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    public Optional<EntryHit> entryAt(int mouseX, int mouseY) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            Optional<EntryHit> hit = windows.get(i).entryHitAt(mouseX, mouseY);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (button != 0) {
            return false;
        }
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window window = windows.get(i);
            if (!window.contains((int) mouseX, (int) mouseY)) {
                continue;
            }
            bringToFront(window);
            if (window.closeContains((int) mouseX, (int) mouseY)) {
                windows.remove(window);
                return true;
            }
            Optional<EntryHit> entry = window.entryHitAt((int) mouseX, (int) mouseY);
            if (entry.isPresent() && NestedContainerAccess.hasOpenableContainer(entry.get().entry().stack())) {
                return open(entry.get().entry().stack(), entry.get().childPath(), (int) mouseX, (int) mouseY,
                        screenWidth, screenHeight);
            }
            if (window.headerContains((int) mouseX, (int) mouseY)) {
                dragging = window;
                dragOffsetX = (int) mouseX - window.x;
                dragOffsetY = (int) mouseY - window.y;
                return true;
            }
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (button != 0 || dragging == null) {
            return false;
        }
        dragging.x = (int) mouseX - dragOffsetX;
        dragging.y = (int) mouseY - dragOffsetY;
        dragging.clamp(screenWidth, screenHeight);
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button != 0 || dragging == null) {
            return false;
        }
        dragging = null;
        return true;
    }

    public boolean isDraggingWindow() {
        return dragging != null;
    }

    private void bringToFront(Window window) {
        windows.remove(window);
        windows.add(window);
    }

    private static final class Window {
        private ItemStack stack;
        private final NestedContainerPath path;
        private final Component title;
        private List<NamedGridInventoryData> containers;
        private List<ContainerView> views;
        private int x;
        private int y;
        private final int width;
        private final int height;

        private Window(ItemStack stack, NestedContainerPath path, Component title, List<NamedGridInventoryData> containers,
                       List<ContainerView> views, int x, int y,
                       int width, int height) {
            this.stack = stack;
            this.path = path;
            this.title = title;
            this.containers = containers;
            this.views = views;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private static Window create(ItemStack stack, NestedContainerPath path, int x, int y) {
            List<NamedGridInventoryData> containers = containers(stack);
            List<ContainerView> views = views(path, containers);
            int contentWidth = 96;
            int contentHeight = 0;
            boolean showSectionTitles = containers.size() > 1;
            for (ContainerView view : views) {
                contentWidth = Math.max(contentWidth, view.gridOuterWidth());
                if (showSectionTitles) {
                    contentHeight += TITLE_HEIGHT;
                }
                contentHeight += view.gridOuterHeight() + CONTAINER_GAP;
            }
            contentHeight = Math.max(24, contentHeight - CONTAINER_GAP);
            return new Window(stack, path, stack.getHoverName(), List.copyOf(containers), views, x, y,
                    contentWidth + PADDING * 2, HEADER_HEIGHT + PADDING * 2 + contentHeight);
        }

        private boolean refresh(Function<NestedContainerPath, Optional<ItemStack>> resolver) {
            if (path.segments().isEmpty()) {
                return true;
            }
            Optional<ItemStack> resolved = resolver.apply(path);
            if (resolved.isEmpty() || !NestedContainerAccess.hasOpenableContainer(resolved.get())) {
                return false;
            }
            if (ItemStack.matches(stack, resolved.get())) {
                return true;
            }
            stack = resolved.get().copy();
            containers = containers(stack);
            views = views(path, containers);
            return true;
        }

        private void render(GuiGraphics graphics, int mouseX, int mouseY, boolean hoverEnabled,
                            Optional<PlacementPreview> preview,
                            HoverAnimationTracker<EntryAnimationKey> hoverAnimations, float z) {
            Font font = Minecraft.getInstance().font;
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, z);
            graphics.fill(x + 2, y, x + width - 2, y + height, 0xEE151922);
            graphics.fill(x, y + 2, x + width, y + height - 2, 0xEE151922);
            graphics.renderOutline(x, y, width, height, contains(mouseX, mouseY) ? 0x88AABEDC : 0x44FFFFFF);
            graphics.fill(x, y, x + width, y + HEADER_HEIGHT, 0xAA202733);
            graphics.renderItem(stack, x + 5, y + 3);
            graphics.drawString(font, font.plainSubstrByWidth(title.getString(), width - 42), x + 24,
                    y + (HEADER_HEIGHT - font.lineHeight) / 2, 0xF1F5F9, false);
            int closeX = closeX();
            int closeY = closeY();
            int closeColor = closeContains(mouseX, mouseY) ? 0xFFE8A0A0 : 0xFFD8E2F0;
            graphics.fill(closeX + 3, closeY + 3, closeX + 5, closeY + 5, closeColor);
            graphics.fill(closeX + 7, closeY + 3, closeX + 9, closeY + 5, closeColor);
            graphics.fill(closeX + 5, closeY + 5, closeX + 7, closeY + 7, closeColor);
            graphics.fill(closeX + 3, closeY + 7, closeX + 5, closeY + 9, closeColor);
            graphics.fill(closeX + 7, closeY + 7, closeX + 9, closeY + 9, closeColor);

            int gridY = y + HEADER_HEIGHT + PADDING;
            boolean showSectionTitles = containers.size() > 1;
            for (ContainerView view : views) {
                GridInventoryData inventory = view.inventory();
                if (showSectionTitles) {
                    graphics.drawString(font, view.title(), x + PADDING, gridY, 0x94A3B8, false);
                    gridY += TITLE_HEIGHT;
                }
                int gridX = gridLeft(view);
                int gridTop = gridY + GRID_BORDER;
                GridRenderer.renderGrid(graphics, gridX, gridTop, inventory, CELL);
                int hoverCellX = -1;
                int hoverCellY = -1;
                if (hoverEnabled) {
                    hoverCellX = GridLayoutMetrics.cellXAt(inventory, mouseX - gridX, mouseY - gridTop, CELL);
                    hoverCellY = GridLayoutMetrics.cellYAt(inventory, mouseX - gridX, mouseY - gridTop, CELL);
                }
                for (EntryView entryView : view.entries()) {
                    GridEntry entry = entryView.entry();
                    boolean hovered = hoverEnabled && entry.contains(hoverCellX, hoverCellY);
                    float hoverProgress = hoverAnimations.update(entryView.key(), hovered);
                    GridItemRenderer.renderEntry(graphics, entry, inventory, gridX, gridTop, CELL, 1.0F,
                            hoverProgress);
                }
                preview.ifPresent(value -> renderPlacementPreview(graphics, value, view, inventory, gridX, gridTop,
                        mouseX, mouseY));
                gridY += view.gridOuterHeight() + CONTAINER_GAP;
            }
            graphics.pose().popPose();
        }

        private void renderPlacementPreview(GuiGraphics graphics, PlacementPreview preview, ContainerView view,
                                            GridInventoryData inventory, int gridX, int gridTop, int mouseX, int mouseY) {
            int anchorX = GridLayoutMetrics.cellXAt(inventory, mouseX - gridX, mouseY - gridTop, CELL);
            int anchorY = GridLayoutMetrics.cellYAt(inventory, mouseX - gridX, mouseY - gridTop, CELL);
            if (anchorX < 0 || anchorY < 0 || !inventory.isEnabledCell(anchorX, anchorY)) {
                return;
            }
            int targetX = anchorX - preview.anchorCellX();
            int targetY = anchorY - preview.anchorCellY();
            GridItemSize size = GridItemSizeManager.getSize(preview.stack());
            int width = size.placedWidth(preview.rotated());
            int height = size.placedHeight(preview.rotated());
            UUID ignoredEntryId = preview.ignoredEntryId(path, view.id()).orElse(null);
            boolean valid = GridPlacementValidator.canPlace(inventory, preview.stack(), targetX, targetY,
                    preview.rotated(), ignoredEntryId, path.depth() + 1);
            int fillColor = valid ? 0x6630C860 : 0x66D84040;
            int outlineColor = valid ? 0xCC7DFFA2 : 0xCCFF8888;
            NestedContainerWindowManager.renderPlacementPreview(graphics, inventory, gridX, gridTop, targetX, targetY, width, height,
                    anchorX, anchorY, fillColor, outlineColor);
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        }

        private boolean headerContains(int mouseX, int mouseY) {
            return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + HEADER_HEIGHT;
        }

        private boolean closeContains(int mouseX, int mouseY) {
            return mouseX >= closeX() && mouseY >= closeY()
                    && mouseX < closeX() + CLOSE_SIZE && mouseY < closeY() + CLOSE_SIZE;
        }

        private Optional<GridHit> gridAt(int mouseX, int mouseY) {
            int gridY = y + HEADER_HEIGHT + PADDING;
            boolean showSectionTitles = containers.size() > 1;
            for (ContainerView view : views) {
                GridInventoryData inventory = view.inventory();
                if (showSectionTitles) {
                    gridY += TITLE_HEIGHT;
                }
                int gridX = gridLeft(view);
                int gridTop = gridY + GRID_BORDER;
                int localX = mouseX - gridX;
                int localY = mouseY - gridTop;
                int cellX = GridLayoutMetrics.cellXAt(inventory, localX, localY, CELL);
                int cellY = GridLayoutMetrics.cellYAt(inventory, localX, localY, CELL);
                if (cellX >= 0 && cellY >= 0 && inventory.isEnabledCell(cellX, cellY)) {
                    return Optional.of(new GridHit(path, view.id(), inventory, cellX, cellY, gridX, gridTop));
                }
                gridY += view.gridOuterHeight() + CONTAINER_GAP;
            }
            return Optional.empty();
        }

        private Optional<EntryHit> entryHitAt(int mouseX, int mouseY) {
            return gridAt(mouseX, mouseY).flatMap(hit -> hit.inventory().getEntries().stream()
                    .filter(candidate -> candidate.contains(hit.cellX(), hit.cellY()))
                    .findFirst()
                    .map(entry -> new EntryHit(hit.ownerPath(), hit.containerId(), hit.inventory(), entry,
                            hit.drawX(entry.x(), entry.y()), hit.drawY(entry.x(), entry.y()))));
        }

        private int closeX() {
            return x + width - CLOSE_SIZE - 5;
        }

        private int closeY() {
            return y + (HEADER_HEIGHT - CLOSE_SIZE) / 2;
        }

        private void clamp(int screenWidth, int screenHeight) {
            x = Math.max(4, Math.min(x, Math.max(4, screenWidth - width - 4)));
            y = Math.max(4, Math.min(y, Math.max(4, screenHeight - height - 4)));
        }

        private int gridLeft(ContainerView view) {
            return x + PADDING + GRID_BORDER
                    + Math.max(0, (width - PADDING * 2 - view.gridOuterWidth()) / 2);
        }
    }

    private static void renderPlacementPreview(GuiGraphics graphics, GridInventoryData inventory,
                                               int left, int top, int targetX, int targetY, int width, int height,
                                               int anchorX, int anchorY,
                                               int fillColor, int outlineColor) {
        String originSection = inventory.sectionAt(anchorX, anchorY);
        boolean[][] occupied = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellX = targetX + x;
                int cellY = targetY + y;
                if (originSection == null || !originSection.equals(inventory.sectionAt(cellX, cellY))) {
                    continue;
                }
                occupied[y][x] = true;
                int drawX = left + GridLayoutMetrics.cellLeft(inventory, cellX, cellY, CELL);
                int drawY = top + GridLayoutMetrics.cellTop(inventory, cellX, cellY, CELL);
                graphics.fill(drawX, drawY, drawX + CELL, drawY + CELL, fillColor);
            }
        }
        renderOccupiedOuterOutline(graphics, inventory, left, top, targetX, targetY, occupied, outlineColor);
    }

    private static void renderOccupiedOuterOutline(GuiGraphics graphics, GridInventoryData inventory, int left, int top,
                                                   int targetX, int targetY, boolean[][] occupied, int color) {
        for (int y = 0; y < occupied.length; y++) {
            for (int x = 0; x < occupied[y].length; x++) {
                if (!occupied[y][x]) {
                    continue;
                }
                int cellX = targetX + x;
                int cellY = targetY + y;
                int drawX = left + GridLayoutMetrics.cellLeft(inventory, cellX, cellY, CELL);
                int drawY = top + GridLayoutMetrics.cellTop(inventory, cellX, cellY, CELL);
                if (!isOccupied(occupied, x - 1, y)) {
                    graphics.fill(drawX, drawY, drawX + 1, drawY + CELL, color);
                }
                if (!isOccupied(occupied, x + 1, y)) {
                    graphics.fill(drawX + CELL - 1, drawY, drawX + CELL, drawY + CELL, color);
                }
                if (!isOccupied(occupied, x, y - 1)) {
                    graphics.fill(drawX, drawY, drawX + CELL, drawY + 1, color);
                }
                if (!isOccupied(occupied, x, y + 1)) {
                    graphics.fill(drawX, drawY + CELL - 1, drawX + CELL, drawY + CELL, color);
                }
            }
        }
    }

    private static boolean isOccupied(boolean[][] occupied, int x, int y) {
        return y >= 0 && y < occupied.length && x >= 0 && x < occupied[y].length && occupied[y][x];
    }

    private static List<NamedGridInventoryData> containers(ItemStack stack) {
        List<NamedGridInventoryData> containers = new ArrayList<>();
        NestedContainerAccess.grid(stack)
                .ifPresent(grid -> containers.add(new NamedGridInventoryData("", "", grid)));
        NestedContainerAccess.equipmentStorage(stack)
                .ifPresent(value -> value.containers().forEach(container -> containers.add(
                        new NamedGridInventoryData(container.id(), container.title(), container.inventory().copy()))));
        return List.copyOf(containers);
    }

    private static List<ContainerView> views(NestedContainerPath path, List<NamedGridInventoryData> containers) {
        return containers.stream()
                .map(container -> new ContainerView(
                        container.id(),
                        Component.literal(container.title()),
                        container.inventory(),
                        GridLayoutMetrics.width(container.inventory(), CELL) + GRID_BORDER * 2,
                        GridLayoutMetrics.height(container.inventory(), CELL) + GRID_BORDER * 2,
                        container.inventory().getEntries().stream()
                                .map(entry -> new EntryView(entry,
                                        new EntryAnimationKey(path, container.id(), entry.entryId())))
                                .toList()))
                .toList();
    }

    public record GridHit(NestedContainerPath ownerPath, String containerId, GridInventoryData inventory, int cellX, int cellY,
                          int gridLeft, int gridTop) {
        public int drawX(int cellX, int cellY) {
            return gridLeft + GridLayoutMetrics.cellLeft(inventory, cellX, cellY, CELL);
        }

        public int drawY(int cellX, int cellY) {
            return gridTop + GridLayoutMetrics.cellTop(inventory, cellX, cellY, CELL);
        }
    }

    public record EntryHit(NestedContainerPath ownerPath, String containerId, GridInventoryData inventory, GridEntry entry,
                           int drawX, int drawY) {
        public NestedContainerPath childPath() {
            return containerId.isEmpty()
                    ? ownerPath.gridEntry(entry.entryId())
                    : ownerPath.containerEntry(containerId, entry.entryId());
        }
    }

    public record PlacementPreview(ItemStack stack, boolean rotated, int anchorCellX, int anchorCellY,
                                   Optional<NestedContainerPath> sourceOwnerPath,
                                   Optional<String> sourceContainerId,
                                   Optional<UUID> sourceEntryId) {
        public PlacementPreview {
            stack = stack.copy();
            sourceOwnerPath = sourceOwnerPath == null ? Optional.empty() : sourceOwnerPath;
            sourceContainerId = sourceContainerId == null ? Optional.empty() : sourceContainerId;
            sourceEntryId = sourceEntryId == null ? Optional.empty() : sourceEntryId;
        }

        private Optional<UUID> ignoredEntryId(NestedContainerPath targetOwnerPath, String targetContainerId) {
            if (sourceOwnerPath.isPresent()
                    && sourceContainerId.isPresent()
                    && sourceEntryId.isPresent()
                    && sourceOwnerPath.get().equals(targetOwnerPath)
                    && sourceContainerId.get().equals(targetContainerId)) {
                return sourceEntryId;
            }
            return Optional.empty();
        }
    }

    private record EntryAnimationKey(NestedContainerPath path, String containerId, UUID entryId) {
    }

    private record ContainerView(String id, Component title, GridInventoryData inventory, int gridOuterWidth,
                                 int gridOuterHeight, List<EntryView> entries) {
    }

    private record EntryView(GridEntry entry, EntryAnimationKey key) {
    }
}
