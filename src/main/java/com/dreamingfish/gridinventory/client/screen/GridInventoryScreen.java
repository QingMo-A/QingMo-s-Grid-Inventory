package com.dreamingfish.gridinventory.client.screen;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.ExtractToPlayerInventoryPacket;
import com.dreamingfish.gridinventory.common.network.ExtractGridEntryToPlayerSlotPacket;
import com.dreamingfish.gridinventory.common.network.InsertFromPlayerInventoryPacket;
import com.dreamingfish.gridinventory.common.network.MoveGridEntryPacket;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class GridInventoryScreen extends AbstractContainerScreen<GridInventoryMenu> {
    private static final int CELL = 18;
    private int gridLeft;
    private int gridTop;
    private GridEntry draggingEntry;
    private boolean rotatedPreview;
    private int lastPlayerSlot = -1;
    private ItemStack selectedPlayerStack = ItemStack.EMPTY;
    private final NearbyItemsPanel nearbyItemsPanel = new NearbyItemsPanel();

    public GridInventoryScreen(GridInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = 126;
    }

    @Override
    protected void init() {
        super.init();
        gridLeft = leftPos + 16;
        gridTop = topPos + 18;
        nearbyItemsPanel.setBounds(leftPos + imageWidth + 8, topPos + 16);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF202020);
        GridRenderer.renderGrid(graphics, gridLeft, gridTop, menu.getGridData().getColumns(), menu.getGridData().getRows(), CELL);
        for (GridEntry entry : menu.getGridData().getEntries()) {
            if (draggingEntry != null && draggingEntry.entryId().equals(entry.entryId())) {
                continue;
            }
            GridItemRenderer.renderEntry(graphics, entry, gridLeft, gridTop, CELL);
        }
        renderDraggedOriginShadow(graphics);
        renderHover(graphics, mouseX, mouseY);
        renderPreview(graphics, mouseX, mouseY);
        renderDraggedStackGhost(graphics, mouseX, mouseY);
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        if (draggingEntry != null || !selectedPlayerStack.isEmpty()) {
            return;
        }
        entryAt(mouseX, mouseY).ifPresent(entry -> {
            int x1 = gridLeft + entry.x() * CELL;
            int y1 = gridTop + entry.y() * CELL;
            int x2 = x1 + entry.width() * CELL;
            int y2 = y1 + entry.height() * CELL;
            graphics.fill(x1, y1, x2, y2, 0x22FFFFFF);
            graphics.renderOutline(x1, y1, x2 - x1, y2 - y1, 0xFFFFFFFF);
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        nearbyItemsPanel.render(graphics, mouseX, mouseY);
        entryAt(mouseX, mouseY).ifPresent(entry -> graphics.renderTooltip(font, List.of(
                entry.stack().getHoverName(),
                Component.literal("Size: " + entry.width() + " x " + entry.height()).withStyle(ChatFormatting.GRAY),
                Component.literal("Rotatable: " + (GridItemSizeManager.getSize(entry.stack()).rotatable() ? "Yes" : "No")).withStyle(ChatFormatting.GRAY)
        ), Optional.empty(), mouseX, mouseY));
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggingEntry != null ? draggingEntry.stack() : selectedPlayerStack;
        if (stack.isEmpty() || !inGrid(mouseX, mouseY)) {
            return;
        }
        int x = cellX(mouseX);
        int y = cellY(mouseY);
        boolean valid = GridPlacementValidator.canPlace(menu.getGridData(), stack, x, y, rotatedPreview, draggingEntry == null ? null : draggingEntry.entryId());
        GridItemSize size = GridItemSizeManager.getSize(stack);
        int w = size.placedWidth(rotatedPreview);
        int h = size.placedHeight(rotatedPreview);
        int left = gridLeft + x * CELL;
        int top = gridTop + y * CELL;
        int color = valid ? 0x6630C860 : 0x66D84040;
        int outline = valid ? 0xCC7DFFA2 : 0xCCFF8888;
        graphics.fill(left, top, left + w * CELL, top + h * CELL, color);
        renderCellOutlines(graphics, left, top, w, h, outline);
    }

    private void renderDraggedOriginShadow(GuiGraphics graphics) {
        if (draggingEntry == null) {
            return;
        }
        GridItemRenderer.renderEntry(graphics, draggingEntry, gridLeft, gridTop, CELL, 0.35F);
    }

    private void renderDraggedStackGhost(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggingEntry != null ? draggingEntry.stack() : selectedPlayerStack;
        if (stack.isEmpty()) {
            return;
        }
        GridItemSize size = GridItemSizeManager.getSize(stack);
        int w = size.placedWidth(rotatedPreview);
        int h = size.placedHeight(rotatedPreview);
        int left = inGrid(mouseX, mouseY) ? gridLeft + cellX(mouseX) * CELL : mouseX - 8;
        int top = inGrid(mouseX, mouseY) ? gridTop + cellY(mouseY) * CELL : mouseY - 8;
        graphics.fill(left, top, left + w * CELL, top + h * CELL, 0x332C6DB8);
        renderCellOutlines(graphics, left, top, w, h, 0x99FFFFFF);
        GridItemRenderer.renderStack(graphics, stack, left + 4, top + 4, 0.75F);
    }

    private void renderCellOutlines(GuiGraphics graphics, int left, int top, int width, int height, int color) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                graphics.renderOutline(left + x * CELL, top + y * CELL, CELL, CELL, color);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nearbyItemsPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1 && (draggingEntry != null || !selectedPlayerStack.isEmpty())) {
            rotatedPreview = !rotatedPreview;
            return true;
        }
        if (inGrid((int) mouseX, (int) mouseY)) {
            int x = cellX((int) mouseX);
            int y = cellY((int) mouseY);
            if (button == 1) {
                rotatedPreview = !rotatedPreview;
                return true;
            }
            Optional<GridEntry> hit = menu.getGridData().getEntries().stream().filter(entry -> entry.contains(x, y)).findFirst();
            if (hit.isPresent()) {
                if (hasShiftDown()) {
                    PacketDistributor.sendToServer(new ExtractToPlayerInventoryPacket(hit.get().entryId(), hit.get().stack().getCount()));
                } else {
                    draggingEntry = hit.get();
                    rotatedPreview = hit.get().rotated();
                }
                return true;
            }
        }
        Slot hovered = findHoveredSlot(mouseX, mouseY);
        if (hovered != null) {
            lastPlayerSlot = hovered.getSlotIndex();
            if (hasShiftDown() && button == 0) {
                PacketDistributor.sendToServer(new InsertFromPlayerInventoryPacket(lastPlayerSlot, 0, 0, false, true));
                selectedPlayerStack = ItemStack.EMPTY;
                return true;
            }
            if (button == 0 && hovered.hasItem()) {
                selectedPlayerStack = hovered.getItem().copy();
                rotatedPreview = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingEntry != null) {
            if (inGrid((int) mouseX, (int) mouseY)) {
                PacketDistributor.sendToServer(new MoveGridEntryPacket(draggingEntry.entryId(), cellX((int) mouseX), cellY((int) mouseY), rotatedPreview));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    PacketDistributor.sendToServer(new ExtractGridEntryToPlayerSlotPacket(draggingEntry.entryId(), hovered.getSlotIndex(), draggingEntry.stack().getCount()));
                }
            }
            clearDragState();
            return true;
        }
        if (button == 0 && !selectedPlayerStack.isEmpty() && lastPlayerSlot >= 0) {
            if (inGrid((int) mouseX, (int) mouseY)) {
                PacketDistributor.sendToServer(new InsertFromPlayerInventoryPacket(lastPlayerSlot, cellX((int) mouseX), cellY((int) mouseY), rotatedPreview, false));
            }
            clearDragState();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (nearbyItemsPanel.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private Optional<GridEntry> entryAt(int mouseX, int mouseY) {
        if (!inGrid(mouseX, mouseY)) {
            return Optional.empty();
        }
        int x = cellX(mouseX);
        int y = cellY(mouseY);
        return menu.getGridData().getEntries().stream().filter(entry -> entry.contains(x, y)).findFirst();
    }

    private boolean inGrid(int mouseX, int mouseY) {
        return mouseX >= gridLeft && mouseY >= gridTop && mouseX < gridLeft + menu.getGridData().getColumns() * CELL && mouseY < gridTop + menu.getGridData().getRows() * CELL;
    }

    private int cellX(int mouseX) {
        return (mouseX - gridLeft) / CELL;
    }

    private int cellY(int mouseY) {
        return (mouseY - gridTop) / CELL;
    }

    private Slot findHoveredSlot(double mouseX, double mouseY) {
        for (Slot slot : menu.slots) {
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private void clearDragState() {
        draggingEntry = null;
        selectedPlayerStack = ItemStack.EMPTY;
        lastPlayerSlot = -1;
        rotatedPreview = false;
    }
}
