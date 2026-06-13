package com.dreamingfish.gridinventory.client.screen.widget;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.client.pickup.ClientPickupController;
import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NearbyItemsPanel {
    private static final int CELL = 18;
    private final List<Entry> entries = new ArrayList<>();
    private int left;
    private int top;
    private int columns;
    private int visibleRows;
    private int panelHeight;
    private int scrollRows;
    private int totalRows;
    private NearbyGroundItemView draggedView;
    private boolean dragging;

    public void setBounds(int left, int top) {
        setBounds(left, top, GridInventoryServices.clientConfig().nearbyPanelVisibleRows() * CELL + 26);
    }

    public void setBounds(int left, int top, int availableHeight) {
        setBounds(left, top, availableHeight, GridInventoryServices.clientConfig().nearbyPanelColumns());
    }

    public void setBounds(int left, int top, int availableHeight, int maxColumns) {
        this.left = left;
        this.top = top;
        this.columns = Math.max(2, Math.min(GridInventoryServices.clientConfig().nearbyPanelColumns(), maxColumns));
        this.panelHeight = Math.max(CELL + 26, availableHeight);
        this.visibleRows = Math.max(1, (panelHeight - 26) / CELL);
    }

    public int width() {
        return columns * CELL + 12;
    }

    public int height() {
        return panelHeight > 0 ? panelHeight : visibleRows * CELL + 26;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!GridInventoryServices.clientConfig().showNearbyItemsPanel()) {
            return;
        }
        refresh();
        Font font = Minecraft.getInstance().font;
        graphics.fill(left, top, left + width(), top + height(), 0x2E171717);
        graphics.fill(left, top, left + width(), top + 14, 0x002D2D2D);
        graphics.drawString(font, Component.translatable("screen.df_grid_inventory.nearby_items"), left + 5, top + 4, 0xFFFFFF, false);

        int gridLeft = left + 6;
        int gridTop = top + 20;
        graphics.enableScissor(gridLeft, gridTop, gridLeft + columns * CELL, gridTop + visibleRows * CELL);
        for (Entry entry : entries) {
            int drawY = gridTop + (entry.y - scrollRows) * CELL;
            if (entry.y + entry.view.gridHeight() <= scrollRows || entry.y >= scrollRows + visibleRows) {
                continue;
            }
            int drawX = gridLeft + entry.x * CELL;
            int color = isInside(mouseX, mouseY, drawX, drawY, entry.view.gridWidth() * CELL, entry.view.gridHeight() * CELL) ? 0x994D6EA8 : 0x77333333;
            int areaWidth = entry.view.gridWidth() * CELL;
            int areaHeight = entry.view.gridHeight() * CELL;
            boolean draggedOrigin = dragging && draggedView != null && draggedView.entityId() == entry.view.entityId();
            graphics.fill(drawX, drawY, drawX + areaWidth, drawY + areaHeight, draggedOrigin ? 0x44333333 : color);
            GridItemRenderer.renderStackInArea(graphics, entry.view.stack(), drawX, drawY, areaWidth, areaHeight, draggedOrigin ? 0.35F : 1.0F);
        }
        graphics.disableScissor();

        if (!dragging) {
            hovered(mouseX, mouseY).ifPresent(entry -> graphics.renderTooltip(font, List.of(
                entry.view.stack().getHoverName(),
                Component.literal("Count: " + entry.view.stack().getCount()).withStyle(ChatFormatting.GRAY),
                Component.literal(String.format("Distance: %.1f", entry.view.distance())).withStyle(ChatFormatting.GRAY),
                Component.literal("Size: " + entry.view.gridWidth() + " x " + entry.view.gridHeight()).withStyle(ChatFormatting.GRAY),
                Component.translatable("tooltip.df_grid_inventory.nearby_drag_hint").withStyle(ChatFormatting.YELLOW)
            ), Optional.empty(), mouseX, mouseY));
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !GridInventoryServices.clientConfig().showNearbyItemsPanel()) {
            return false;
        }
        Optional<Entry> hit = hovered((int) mouseX, (int) mouseY);
        hit.ifPresent(entry -> {
            draggedView = entry.view;
            dragging = true;
        });
        return hit.isPresent() || isInside((int) mouseX, (int) mouseY, left, top, width(), height());
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return button == 0 && dragging;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !dragging) {
            return false;
        }
        return true;
    }

    public boolean pickupHovered(int mouseX, int mouseY) {
        Optional<Entry> hit = hovered(mouseX, mouseY);
        hit.ifPresent(entry -> ClientPickupController.requestPickup(entry.view.entityId()));
        return hit.isPresent();
    }

    public boolean isDraggingGroundItem() {
        return dragging && draggedView != null;
    }

    public Optional<NearbyGroundItemView> draggedView() {
        return Optional.ofNullable(draggedView);
    }

    public void clearDrag() {
        draggedView = null;
        dragging = false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!isInside((int) mouseX, (int) mouseY, left, top, width(), height())) {
            return false;
        }
        int max = Math.max(0, totalRows - visibleRows);
        scrollRows = Math.max(0, Math.min(max, scrollRows - (int) Math.signum(deltaY)));
        return true;
    }

    private Optional<Entry> hovered(int mouseX, int mouseY) {
        int gridLeft = left + 6;
        int gridTop = top + 20;
        for (Entry entry : entries) {
            int drawX = gridLeft + entry.x * CELL;
            int drawY = gridTop + (entry.y - scrollRows) * CELL;
            if (isInside(mouseX, mouseY, drawX, drawY, entry.view.gridWidth() * CELL, entry.view.gridHeight() * CELL)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        entries.clear();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        double range = effectiveNearbyRange();
        AABB area = minecraft.player.getBoundingBox().inflate(range);
        List<NearbyGroundItemView> views = minecraft.level.getEntitiesOfClass(ItemEntity.class, area, item -> !item.isRemoved() && item.isAlive() && !item.getItem().isEmpty())
                .stream()
                .map(item -> {
                    ItemStack stack = item.getItem();
                    var size = GridItemSizeManager.getSize(stack);
                    return new NearbyGroundItemView(item.getId(), stack.copy(), item.distanceTo(minecraft.player), size.width(), size.height(), size.rotatable(), item.position());
                })
                .sorted(Comparator.comparingDouble(NearbyGroundItemView::distance))
                .toList();
        pack(views);
    }

    private static double effectiveNearbyRange() {
        double range = GridInventoryServices.config().nearbyItemsRange();
        if (GridInventoryServices.config().serverValidateNearbyRange()) {
            range = Math.min(range, GridInventoryServices.config().pickupRange());
        }
        return range;
    }

    private void pack(List<NearbyGroundItemView> views) {
        List<boolean[]> occupied = new ArrayList<>();
        totalRows = visibleRows;
        for (NearbyGroundItemView view : views) {
            boolean placed = false;
            for (int y = 0; !placed && y < 128; y++) {
                ensureRows(occupied, y + view.gridHeight());
                for (int x = 0; x <= columns - view.gridWidth(); x++) {
                    if (fits(occupied, x, y, view.gridWidth(), view.gridHeight())) {
                        occupy(occupied, x, y, view.gridWidth(), view.gridHeight());
                        entries.add(new Entry(view, x, y));
                        totalRows = Math.max(totalRows, y + view.gridHeight());
                        placed = true;
                        break;
                    }
                }
            }
        }
    }

    private void ensureRows(List<boolean[]> occupied, int rows) {
        while (occupied.size() < rows) {
            occupied.add(new boolean[columns]);
        }
    }

    private boolean fits(List<boolean[]> occupied, int x, int y, int w, int h) {
        for (int yy = y; yy < y + h; yy++) {
            for (int xx = x; xx < x + w; xx++) {
                if (occupied.get(yy)[xx]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void occupy(List<boolean[]> occupied, int x, int y, int w, int h) {
        for (int yy = y; yy < y + h; yy++) {
            for (int xx = x; xx < x + w; xx++) {
                occupied.get(yy)[xx] = true;
            }
        }
    }

    private static boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private record Entry(NearbyGroundItemView view, int x, int y) {
    }
}
