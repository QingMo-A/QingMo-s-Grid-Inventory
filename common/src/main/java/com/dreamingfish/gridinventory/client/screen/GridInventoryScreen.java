package com.dreamingfish.gridinventory.client.screen;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.render.EquipmentStorageTooltipRenderer;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.ExtractToPlayerInventoryMessage;
import com.dreamingfish.gridinventory.common.network.ExtractGridEntryToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.InsertFromPlayerInventoryMessage;
import com.dreamingfish.gridinventory.common.network.MoveGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.InsertIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.MoveEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.ExtractEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.TransferGridEntryIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryIntoGridMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.MovePlayerFreeSlotMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.DropGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.DropEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.ToggleGridEntryBackpackFoldMessage;
import com.dreamingfish.gridinventory.common.network.ToggleEquipmentStorageEntryBackpackFoldMessage;
import com.dreamingfish.gridinventory.common.network.PickupGroundItemIntoGridMessage;
import com.dreamingfish.gridinventory.common.network.PickupGroundItemIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.InsertPlayerSlotIntoCurioMessage;
import com.dreamingfish.gridinventory.common.network.InsertGridEntryIntoCurioMessage;
import com.dreamingfish.gridinventory.common.network.InsertEquipmentStorageEntryIntoCurioMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToGridMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyGroundItemView;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import com.dreamingfish.gridinventory.client.screen.panel.EquipmentColumnPanel;
import com.dreamingfish.gridinventory.client.screen.panel.GridColumnPanel;
import com.dreamingfish.gridinventory.client.screen.widget.FreeSlotWidget;
import com.dreamingfish.gridinventory.client.screen.widget.CuriosSlotWidget;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.access.SlotPositionAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class GridInventoryScreen extends AbstractContainerScreen<GridInventoryMenu> {
    private static final int CELL = 27;
    private int gridLeft;
    private int gridTop;
    private GridEntry draggingEntry;
    private GridColumnPanel.EquipmentEntryHit draggingEquipmentEntry;
    private CuriosSlotWidget draggingCurioSlot;
    private ItemStack dragPreviewStack = ItemStack.EMPTY;
    private boolean rotatedPreview;
    private int lastPlayerSlot = -1;
    private ItemStack selectedPlayerStack = ItemStack.EMPTY;
    private int dragAnchorCellX;
    private int dragAnchorCellY;
    private int dragAnchorPixelX = CELL / 2;
    private int dragAnchorPixelY = CELL / 2;
    private final NearbyItemsPanel nearbyItemsPanel = new NearbyItemsPanel();
    private final EquipmentColumnPanel equipmentColumnPanel = new EquipmentColumnPanel();
    private final GridColumnPanel gridColumnPanel = new GridColumnPanel();
    private int workspaceMargin;
    private int columnGap;
    private int equipmentWidth;
    private int gridColumnWidth;
    private int workspaceTop;
    private int columnHeight;

    public GridInventoryScreen(GridInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = menu.isPlayerGrid() ? 390 : 176;
        this.imageHeight = 222;
        this.inventoryLabelY = 126;
    }

    @Override
    protected void init() {
        if (menu.isPlayerGrid()) {
            workspaceMargin = Math.max(6, Math.min(16, Math.min(width, height) / 20));
            columnGap = GridInventoryServices.clientConfig().columnGap();
            imageWidth = Math.max(360, width - workspaceMargin * 2);
            imageHeight = Math.max(222, height - workspaceMargin * 2);
        }
        super.init();
        if (menu.isPlayerGrid()) {
            workspaceTop = topPos;
            columnHeight = imageHeight - 38;
            int nearbyColumns = Math.max(2, Math.min(
                    GridInventoryServices.clientConfig().nearbyPanelColumns(),
                    (imageWidth - 204 - 88 - columnGap * 2 - 12) / CELL
            ));
            int nearbyWidth = nearbyColumns * CELL + 12;
            equipmentWidth = Math.max(204, Math.min(232, imageWidth - nearbyWidth - columnGap * 2 - 88));
            gridColumnWidth = imageWidth - equipmentWidth - nearbyWidth - columnGap * 2;
            int gridColumnLeft = leftPos + equipmentWidth + columnGap;
            int nearbyLeft = gridColumnLeft + gridColumnWidth + columnGap;
            equipmentColumnPanel.setBounds(leftPos, workspaceTop, equipmentWidth, columnHeight);
            gridColumnPanel.setBounds(gridColumnLeft, workspaceTop, gridColumnWidth, columnHeight);
            gridLeft = gridColumnPanel.pocketLeft();
            gridTop = gridColumnPanel.pocketTop();
            nearbyItemsPanel.setBounds(nearbyLeft, workspaceTop, columnHeight, nearbyColumns);
            equipmentColumnPanel.layoutSlots(hotbarTop());
            hideVanillaSlots();
        } else {
            gridLeft = leftPos + 16;
            gridTop = topPos + 18;
            nearbyItemsPanel.setBounds(leftPos + imageWidth + 8, topPos + 16);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (menu.isPlayerGrid()) {
            graphics.fill(leftPos - 4, topPos - 4, leftPos + imageWidth + 4, topPos + imageHeight + 4, 0x2E101010);
            equipmentColumnPanel.render(graphics, mouseX, mouseY, hotbarTop(), menu.slots, lastPlayerSlot,
                    draggedStack(), !draggedStack().isEmpty());
            gridColumnPanel.render(graphics, menu.getGridData(), draggingEntry == null ? null : draggingEntry.entryId(), draggingEquipmentEntry);
            gridLeft = gridColumnPanel.pocketLeft();
            gridTop = gridColumnPanel.pocketTop();
            renderHover(graphics, mouseX, mouseY);
            renderPreview(graphics, mouseX, mouseY);
            renderEquipmentPreview(graphics, mouseX, mouseY);
            renderDraggedStackGhost(graphics, mouseX, mouseY);
            return;
        }
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0x00202020);
        GridRenderer.renderGrid(graphics, gridLeft, gridTop, menu.getGridData(), CELL);
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

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.isPlayerGrid()) {
            super.renderLabels(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (!menu.isPlayerGrid()) {
            super.renderSlot(graphics, slot);
        }
    }

    private void hideVanillaSlots() {
        for (int menuIndex = 0; menuIndex < menu.slots.size(); menuIndex++) {
            setSlotPosition(menuIndex, leftPos - 1000 - menuIndex * CELL, topPos - 1000);
        }
    }

    private int hotbarTop() {
        return topPos + columnHeight - 24;
    }

    private void setSlotPosition(int menuIndex, int absoluteX, int absoluteY) {
        SlotPositionAccessor accessor = (SlotPositionAccessor) menu.slots.get(menuIndex);
        accessor.df_grid_inventory$setX(absoluteX - leftPos);
        accessor.df_grid_inventory$setY(absoluteY - topPos);
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        if (draggingEntry != null || draggingEquipmentEntry != null || draggingCurioSlot != null || !selectedPlayerStack.isEmpty()) {
            return;
        }
        if (menu.isPlayerGrid()) {
            Optional<GridColumnPanel.EquipmentEntryHit> equipmentHit = gridColumnPanel.equipmentEntryAt(mouseX, mouseY);
            if (equipmentHit.isPresent()) {
                GridColumnPanel.EquipmentEntryHit hit = equipmentHit.get();
                GridEntry entry = hit.entry();
                int x1 = hit.region().drawX(entry.x(), entry.y());
                int y1 = hit.region().drawY(entry.x(), entry.y());
                int width = hit.region().areaWidth(entry.x(), entry.y(), entry.width());
                int height = hit.region().areaHeight(entry.x(), entry.y(), entry.height());
                graphics.fill(x1, y1, x1 + width, y1 + height, 0x22FFFFFF);
                renderRegionCellOutlines(graphics, hit.region(), entry.x(), entry.y(), entry.width(), entry.height(), 0xFFFFFFFF);
                return;
            }
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
        graphics.fill(0, 0, width, height, 0x1A000000);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        nearbyItemsPanel.render(graphics, mouseX, mouseY);
        hoveredGridEntry(mouseX, mouseY).ifPresent(entry -> graphics.renderTooltip(font, gridEntryTooltip(entry), Optional.empty(), mouseX, mouseY));
        if (menu.isPlayerGrid() && draggingEntry == null && draggingEquipmentEntry == null && draggingCurioSlot == null && selectedPlayerStack.isEmpty()) {
            equipmentColumnPanel.slotAt(mouseX, mouseY)
                    .map(slot -> menu.slots.get(slot.menuIndex()))
                    .filter(Slot::hasItem)
                    .ifPresent(slot -> graphics.renderTooltip(font, tooltipWithQuickEquipHint(slot.getItem()), Optional.empty(), mouseX, mouseY));
            equipmentColumnPanel.curioSlotAt(mouseX, mouseY)
                    .ifPresent(slot -> graphics.renderTooltip(font, slot.tooltip(), Optional.empty(), mouseX, mouseY));
        }
        if (draggingEntry == null && draggingEquipmentEntry == null && selectedPlayerStack.isEmpty()) {
            hoveredStack(mouseX, mouseY).ifPresent(stack ->
                    EquipmentStorageTooltipRenderer.render(graphics, stack, mouseX, mouseY, width, height));
        }
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggedStack();
        if (stack.isEmpty() || !inGrid(mouseX, mouseY)) {
            return;
        }
        int x = targetGridX(stack, mouseX);
        int y = targetGridY(stack, mouseY);
        boolean valid = GridPlacementValidator.canPlace(menu.getGridData(), stack, x, y, rotatedPreview,
                draggingEntry == null ? null : draggingEntry.entryId());
        GridItemSize size = GridItemSizeManager.getSize(stack);
        int w = size.placedWidth(rotatedPreview);
        int h = size.placedHeight(rotatedPreview);
        int color = valid ? 0x6630C860 : 0x66D84040;
        int outline = valid ? 0xCC7DFFA2 : 0xCCFF8888;
        renderPlacementPreview(graphics, menu.getGridData(), gridLeft, gridTop, x, y, w, h,
                cellX(mouseX, mouseY), cellY(mouseX, mouseY), color, outline);
    }

    private void renderDraggedOriginShadow(GuiGraphics graphics) {
        if (draggingEntry == null) {
            return;
        }
        GridItemRenderer.renderEntry(graphics, draggingEntry, gridLeft, gridTop, CELL, 0.35F);
    }

    private void renderDraggedStackGhost(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggedStack();
        if (stack.isEmpty()) {
            return;
        }
        GridItemSize size = GridItemSizeManager.getSize(stack);
        int w = size.placedWidth(rotatedPreview);
        int h = size.placedHeight(rotatedPreview);
        int left = mouseX - anchorCellX(stack) * CELL - dragAnchorPixelX;
        int top = mouseY - anchorCellY(stack) * CELL - dragAnchorPixelY;
        GridItemRenderer.renderStackInArea(graphics, stack, left, top, w * CELL, h * CELL, 0.75F, rotatedPreview);
    }

    private void renderEquipmentPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggedStack();
        if (stack.isEmpty()) {
            return;
        }
        gridColumnPanel.equipmentRegionAt(mouseX, mouseY).ifPresent(region -> {
            int x = targetRegionX(region, stack, mouseX);
            int y = targetRegionY(region, stack, mouseY);
            boolean movingWithinSameRegion = draggingEquipmentEntry != null
                    && region.slot() == draggingEquipmentEntry.slot()
                    && region.containerId().equals(draggingEquipmentEntry.containerId());
            boolean valid = GridPlacementValidator.canPlace(region.inventory(), stack, x, y, rotatedPreview,
                    movingWithinSameRegion ? draggingEquipmentEntry.entry().entryId() : null);
            GridItemSize size = GridItemSizeManager.getSize(stack);
            int w = size.placedWidth(rotatedPreview);
            int h = size.placedHeight(rotatedPreview);
            renderPlacementPreview(graphics, region.inventory(), region.left(), region.top(), x, y, w, h,
                    region.cellX(mouseX, mouseY), region.cellY(mouseX, mouseY),
                    valid ? 0x6630C860 : 0x66D84040, valid ? 0xCC7DFFA2 : 0xCCFF8888);
        });
    }

    private ItemStack draggedStack() {
        if (!dragPreviewStack.isEmpty()) {
            return dragPreviewStack;
        }
        return draggingEntry != null ? draggingEntry.stack()
                : draggingEquipmentEntry != null ? draggingEquipmentEntry.entry().stack()
                : draggingCurioSlot != null ? draggingCurioSlot.view().stack()
                : nearbyItemsPanel.draggedView().map(NearbyGroundItemView::stack).orElse(selectedPlayerStack);
    }

    private boolean toggleDraggedBackpackPreview() {
        ItemStack stack = draggedStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof GridBackpackItem)) {
            return false;
        }
        ItemStack toggled = stack.copy();
        if (!GridBackpackItem.toggleFolded(toggled)) {
            return false;
        }
        dragPreviewStack = toggled;
        GridItemSize size = GridItemSizeManager.getSize(toggled);
        if (!size.rotatable()) {
            rotatedPreview = false;
        }
        return true;
    }

    private void rotateDraggedPreview() {
        ItemStack stack = draggedStack();
        if (stack.isEmpty()) {
            return;
        }
        GridItemSize size = GridItemSizeManager.getSize(stack);
        if (size.rotatable() && size.width() != size.height()) {
            rotatedPreview = !rotatedPreview;
        }
    }

    private void renderCellOutlines(GuiGraphics graphics, int left, int top, int width, int height, int color) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                graphics.renderOutline(left + x * CELL, top + y * CELL, CELL, CELL, color);
            }
        }
    }

    private void renderPlacementPreview(GuiGraphics graphics, com.dreamingfish.gridinventory.common.data.GridInventoryData inventory,
                                        int left, int top, int targetX, int targetY, int width, int height,
                                        int anchorX, int anchorY,
                                        int fillColor, int outlineColor) {
        String originSection = inventory.sectionAt(anchorX, anchorY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellX = targetX + x;
                int cellY = targetY + y;
                if (originSection == null || !originSection.equals(inventory.sectionAt(cellX, cellY))) {
                    continue;
                }
                int drawX = left + GridLayoutMetrics.cellLeft(inventory, cellX, cellY, CELL);
                int drawY = top + GridLayoutMetrics.cellTop(inventory, cellX, cellY, CELL);
                graphics.fill(drawX, drawY, drawX + CELL, drawY + CELL, fillColor);
                graphics.renderOutline(drawX, drawY, CELL, CELL, outlineColor);
            }
        }
    }

    private void renderRegionCellOutlines(GuiGraphics graphics, GridColumnPanel.Region region, int targetX, int targetY,
                                          int width, int height, int color) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                graphics.renderOutline(region.drawX(targetX + x, targetY + y), region.drawY(targetX + x, targetY + y), CELL, CELL, color);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nearbyItemsPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1 && (draggingEntry != null || draggingEquipmentEntry != null || draggingCurioSlot != null || !selectedPlayerStack.isEmpty())) {
            rotateDraggedPreview();
            return true;
        }
        if (menu.isPlayerGrid()) {
            if (equipmentColumnPanel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            Optional<CuriosSlotWidget> curioHit = equipmentColumnPanel.curioSlotAt(mouseX, mouseY);
            if (curioHit.isPresent()) {
                if (button == 0 && !curioHit.get().view().stack().isEmpty()) {
                    draggingCurioSlot = curioHit.get();
                    selectedPlayerStack = ItemStack.EMPTY;
                    lastPlayerSlot = -1;
                    rotatedPreview = false;
                    dragAnchorCellX = 0;
                    dragAnchorCellY = 0;
                    dragAnchorPixelX = CELL / 2;
                    dragAnchorPixelY = CELL / 2;
                    return true;
                }
                return button == 0;
            }
            Optional<GridColumnPanel.EquipmentEntryHit> equipmentHit = gridColumnPanel.equipmentEntryAt((int) mouseX, (int) mouseY);
            if (equipmentHit.isPresent()) {
                if (button == 1) {
                    GridInventoryServices.network().sendToServer(new QuickEquipEquipmentStorageEntryMessage(
                            equipmentHit.get().slot(), equipmentHit.get().containerId(), equipmentHit.get().entry().entryId()));
                    return true;
                }
                if (button == 0) {
                    draggingEquipmentEntry = equipmentHit.get();
                    dragPreviewStack = draggingEquipmentEntry.entry().stack().copy();
                    rotatedPreview = equipmentHit.get().entry().rotated();
                    setGridDragAnchor((int) mouseX, (int) mouseY,
                            draggingEquipmentEntry.region().drawX(draggingEquipmentEntry.entry().x(), draggingEquipmentEntry.entry().y()),
                            draggingEquipmentEntry.region().drawY(draggingEquipmentEntry.entry().x(), draggingEquipmentEntry.entry().y()),
                            draggingEquipmentEntry.entry().width(), draggingEquipmentEntry.entry().height());
                    return true;
                }
            }
        }
        if (inGrid((int) mouseX, (int) mouseY)) {
            int x = cellX((int) mouseX, (int) mouseY);
            int y = cellY((int) mouseX, (int) mouseY);
            Optional<GridEntry> hit = menu.getGridData().getEntries().stream().filter(entry -> entry.contains(x, y)).findFirst();
            if (hit.isPresent()) {
                if (button == 1 && menu.isPlayerGrid()) {
                    GridInventoryServices.network().sendToServer(new QuickEquipGridEntryMessage(hit.get().entryId()));
                } else if (hasShiftDown() && button == 0) {
                    GridInventoryServices.network().sendToServer(new ExtractToPlayerInventoryMessage(hit.get().entryId(), hit.get().stack().getCount()));
                } else if (button == 0) {
                    draggingEntry = hit.get();
                    dragPreviewStack = draggingEntry.stack().copy();
                    rotatedPreview = hit.get().rotated();
                    setGridDragAnchor((int) mouseX, (int) mouseY,
                            gridLeft + draggingEntry.x() * CELL, gridTop + draggingEntry.y() * CELL,
                            draggingEntry.width(), draggingEntry.height());
                } else {
                    return false;
                }
                return true;
            }
        }
        Slot hovered = findHoveredSlot(mouseX, mouseY);
        if (hovered != null) {
            lastPlayerSlot = hovered.index;
            if (hasShiftDown() && button == 0) {
                GridInventoryServices.network().sendToServer(new InsertFromPlayerInventoryMessage(lastPlayerSlot, 0, 0, false, true, false));
                selectedPlayerStack = ItemStack.EMPTY;
                return true;
            }
            if (button == 1 && hovered.hasItem() && menu.isPlayerGrid()) {
                GridInventoryServices.network().sendToServer(new QuickEquipPlayerSlotMessage(lastPlayerSlot));
                return true;
            }
            if (button == 0 && hovered.hasItem()) {
                selectedPlayerStack = hovered.getItem().copy();
                rotatedPreview = false;
                setSlotDragAnchor((int) mouseX, (int) mouseY, hovered);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeyMappings.ROTATE_GRID_ITEM.matches(keyCode, scanCode) && !draggedStack().isEmpty()) {
            rotateDraggedPreview();
            return true;
        }
        if (ModKeyMappings.TOGGLE_BACKPACK_FOLD.matches(keyCode, scanCode) && draggingEntry != null) {
            toggleDraggedBackpackPreview();
            return true;
        }
        if (ModKeyMappings.TOGGLE_BACKPACK_FOLD.matches(keyCode, scanCode) && draggingEquipmentEntry != null) {
            toggleDraggedBackpackPreview();
            return true;
        }
        if (ModKeyMappings.TOGGLE_BACKPACK_FOLD.matches(keyCode, scanCode)
                && (draggingCurioSlot != null || !selectedPlayerStack.isEmpty())) {
            toggleDraggedBackpackPreview();
            return true;
        }
        if (ModKeyMappings.PICKUP_ITEM.matches(keyCode, scanCode)
                && draggingEntry == null && draggingEquipmentEntry == null && draggingCurioSlot == null && selectedPlayerStack.isEmpty()
                && nearbyItemsPanel.pickupHovered(currentMouseX(), currentMouseY())) {
            return true;
        }
        if (ModKeyMappings.DROP_HOVERED_GRID_ITEM.matches(keyCode, scanCode) && draggedStack().isEmpty()) {
            return dropHoveredGridItem();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean dropHoveredGridItem() {
        int mouseX = currentMouseX();
        int mouseY = currentMouseY();
        if (menu.isPlayerGrid()) {
            Optional<GridColumnPanel.EquipmentEntryHit> equipmentHit = gridColumnPanel.equipmentEntryAt(mouseX, mouseY);
            if (equipmentHit.isPresent()) {
                GridColumnPanel.EquipmentEntryHit hit = equipmentHit.get();
                GridInventoryServices.network().sendToServer(new DropEquipmentStorageEntryMessage(hit.slot(), hit.containerId(), hit.entry().entryId()));
                return true;
            }
        }
        Optional<GridEntry> entry = entryAt(mouseX, mouseY);
        if (entry.isPresent()) {
            GridInventoryServices.network().sendToServer(new DropGridEntryMessage(entry.get().entryId()));
            return true;
        }
        return false;
    }

    private int currentMouseX() {
        return (int) (minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth());
    }

    private int currentMouseY() {
        return (int) (minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight());
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && nearbyItemsPanel.isDraggingGroundItem()) {
            handleGroundItemRelease((int) mouseX, (int) mouseY);
            nearbyItemsPanel.clearDrag();
            return true;
        }
        if (nearbyItemsPanel.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && draggingCurioSlot != null) {
            Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region region = equipmentRegion.get();
                GridInventoryServices.network().sendToServer(new ExtractCurioToEquipmentStorageMessage(
                        draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(), region.slot(), region.containerId(),
                        targetRegionX(region, draggedStack(), (int) mouseX),
                        targetRegionY(region, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new ExtractCurioToGridMessage(
                        draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(),
                        targetGridX(draggedStack(), (int) mouseX),
                        targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractCurioToPlayerSlotMessage(
                            draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(), hovered.index));
                }
            }
            clearDragState();
            return true;
        }
        if (button == 0 && draggingEquipmentEntry != null) {
            Optional<CuriosSlotWidget> curioTarget = menu.isPlayerGrid() ? equipmentColumnPanel.curioSlotAt(mouseX, mouseY) : Optional.empty();
            if (curioTarget.isPresent()) {
                GridInventoryServices.network().sendToServer(new InsertEquipmentStorageEntryIntoCurioMessage(
                        draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                        curioTarget.get().view().identifier(), curioTarget.get().view().index()));
                clearDragState();
                return true;
            }
            Optional<GridColumnPanel.Region> region = gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY);
            if (region.isPresent()) {
                GridColumnPanel.Region target = region.get();
                if (target.slot() == draggingEquipmentEntry.slot()
                        && target.containerId().equals(draggingEquipmentEntry.containerId())) {
                    GridInventoryServices.network().sendToServer(new MoveEquipmentStorageEntryMessage(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            targetRegionX(target, draggedStack(), (int) mouseX),
                            targetRegionY(target, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                } else {
                    GridInventoryServices.network().sendToServer(new TransferEquipmentStorageEntryMessage(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            target.slot(), target.containerId(),
                            targetRegionX(target, draggedStack(), (int) mouseX),
                            targetRegionY(target, draggedStack(), (int) mouseY), rotatedPreview));
                }
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new TransferEquipmentStorageEntryIntoGridMessage(
                        draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                        targetGridX(draggedStack(), (int) mouseX),
                        targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractEquipmentStorageEntryMessage(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            hovered.index, draggingEquipmentEntry.entry().stack().getCount()));
                }
            }
            clearDragState();
            return true;
        }
        if (button == 0 && draggingEntry != null) {
            Optional<CuriosSlotWidget> curioTarget = menu.isPlayerGrid() ? equipmentColumnPanel.curioSlotAt(mouseX, mouseY) : Optional.empty();
            if (curioTarget.isPresent()) {
                GridInventoryServices.network().sendToServer(new InsertGridEntryIntoCurioMessage(
                        draggingEntry.entryId(), curioTarget.get().view().identifier(), curioTarget.get().view().index()));
                clearDragState();
                return true;
            }
            Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region target = equipmentRegion.get();
                GridInventoryServices.network().sendToServer(new TransferGridEntryIntoEquipmentStorageMessage(
                        draggingEntry.entryId(), target.slot(), target.containerId(),
                        targetRegionX(target, draggedStack(), (int) mouseX),
                        targetRegionY(target, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new MoveGridEntryMessage(draggingEntry.entryId(),
                        targetGridX(draggedStack(), (int) mouseX), targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractGridEntryToPlayerSlotMessage(draggingEntry.entryId(), hovered.index, draggingEntry.stack().getCount()));
                }
            }
            clearDragState();
            return true;
        }
        if (button == 0 && !selectedPlayerStack.isEmpty() && lastPlayerSlot >= 0) {
            Optional<CuriosSlotWidget> curioTarget = menu.isPlayerGrid() ? equipmentColumnPanel.curioSlotAt(mouseX, mouseY) : Optional.empty();
            if (curioTarget.isPresent()) {
                GridInventoryServices.network().sendToServer(new InsertPlayerSlotIntoCurioMessage(
                        lastPlayerSlot, curioTarget.get().view().identifier(), curioTarget.get().view().index()));
                clearDragState();
                return true;
            }
            Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region region = equipmentRegion.get();
                GridInventoryServices.network().sendToServer(new InsertIntoEquipmentStorageMessage(lastPlayerSlot, region.slot(), region.containerId(),
                        targetRegionX(region, draggedStack(), (int) mouseX),
                        targetRegionY(region, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new InsertFromPlayerInventoryMessage(lastPlayerSlot,
                        targetGridX(draggedStack(), (int) mouseX), targetGridY(draggedStack(), (int) mouseY), rotatedPreview, false, GridBackpackItem.isFolded(draggedStack())));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null && hovered.index != lastPlayerSlot) {
                    GridInventoryServices.network().sendToServer(new MovePlayerFreeSlotMessage(lastPlayerSlot, hovered.index));
                }
            }
            clearDragState();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void handleGroundItemRelease(int mouseX, int mouseY) {
        Optional<NearbyGroundItemView> dragged = nearbyItemsPanel.draggedView();
        if (dragged.isEmpty()) {
            return;
        }
        NearbyGroundItemView view = dragged.get();
        Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid()
                ? gridColumnPanel.equipmentRegionAt(mouseX, mouseY) : Optional.empty();
        if (equipmentRegion.isPresent()) {
            GridColumnPanel.Region region = equipmentRegion.get();
            GridInventoryServices.network().sendToServer(new PickupGroundItemIntoEquipmentStorageMessage(
                    view.entityId(), region.slot(), region.containerId(),
                    targetRegionX(region, view.stack(), mouseX), targetRegionY(region, view.stack(), mouseY), rotatedPreview));
        } else if (inGrid(mouseX, mouseY)) {
            GridInventoryServices.network().sendToServer(new PickupGroundItemIntoGridMessage(
                    view.entityId(), targetGridX(view.stack(), mouseX), targetGridY(view.stack(), mouseY), rotatedPreview));
        }
        rotatedPreview = false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (nearbyItemsPanel.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.isPlayerGrid() && equipmentColumnPanel.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        if (menu.isPlayerGrid() && gridColumnPanel.mouseScrolled(mouseX, mouseY, scrollY)) {
            gridLeft = gridColumnPanel.pocketLeft();
            gridTop = gridColumnPanel.pocketTop();
            return true;
        }
        if (nearbyItemsPanel.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private Optional<GridEntry> entryAt(int mouseX, int mouseY) {
        if (!inGrid(mouseX, mouseY)) {
            return Optional.empty();
        }
        int x = cellX(mouseX, mouseY);
        int y = cellY(mouseX, mouseY);
        return menu.getGridData().getEntries().stream().filter(entry -> entry.contains(x, y)).findFirst();
    }

    private Optional<GridEntry> hoveredGridEntry(int mouseX, int mouseY) {
        Optional<GridEntry> pocketEntry = entryAt(mouseX, mouseY);
        if (pocketEntry.isPresent() || !menu.isPlayerGrid()) {
            return pocketEntry;
        }
        return gridColumnPanel.equipmentEntryAt(mouseX, mouseY).map(GridColumnPanel.EquipmentEntryHit::entry);
    }

    private Optional<ItemStack> hoveredStack(int mouseX, int mouseY) {
        if (menu.isPlayerGrid()) {
            Optional<ItemStack> slotStack = equipmentColumnPanel.slotAt(mouseX, mouseY)
                    .map(slot -> menu.slots.get(slot.menuIndex()))
                    .filter(Slot::hasItem)
                    .map(Slot::getItem);
            if (slotStack.isPresent()) {
                return slotStack;
            }
            Optional<ItemStack> curioStack = equipmentColumnPanel.curioSlotAt(mouseX, mouseY)
                    .map(slot -> slot.view().stack())
                    .filter(stack -> !stack.isEmpty());
            if (curioStack.isPresent()) {
                return curioStack;
            }
        }
        return hoveredGridEntry(mouseX, mouseY).map(GridEntry::stack);
    }

    private List<Component> tooltipWithQuickEquipHint(ItemStack stack) {
        List<Component> tooltip = new java.util.ArrayList<>(getTooltipFromItem(minecraft, stack));
        Component hint = quickEquipTooltip(stack);
        if (hint != null) {
            tooltip.add(hint);
        }
        return tooltip;
    }

    private List<Component> gridEntryTooltip(GridEntry entry) {
        List<Component> tooltip = new java.util.ArrayList<>();
        tooltip.add(entry.stack().getHoverName());
        tooltip.add(Component.literal("Size: " + entry.width() + " x " + entry.height()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Rotatable: " + (GridItemSizeManager.getSize(entry.stack()).rotatable() ? "Yes" : "No")).withStyle(ChatFormatting.GRAY));
        Component hint = quickEquipTooltip(entry.stack());
        if (hint != null) {
            tooltip.add(hint);
        }
        return tooltip;
    }

    private Component quickEquipTooltip(ItemStack stack) {
        if (minecraft.player == null || stack.isEmpty() || !canQuickEquip(stack)) {
            return null;
        }
        return Component.translatable("tooltip.df_grid_inventory.right_click_equip").withStyle(ChatFormatting.GRAY);
    }

    private boolean canQuickEquip(ItemStack stack) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (minecraft.player.getItemBySlot(slot).isEmpty() && EquipmentSlotHelper.canEquip(stack, slot, minecraft.player)) {
                return true;
            }
        }
        return CuriosIntegration.canQuickEquip(minecraft.player, stack);
    }

    private boolean inGrid(int mouseX, int mouseY) {
        return mouseX >= gridLeft && mouseY >= gridTop
                && mouseX < gridLeft + GridLayoutMetrics.width(menu.getGridData(), CELL)
                && mouseY < gridTop + GridLayoutMetrics.height(menu.getGridData(), CELL)
                && cellX(mouseX, mouseY) >= 0 && cellY(mouseX, mouseY) >= 0;
    }

    private int cellX(int mouseX) {
        return GridLayoutMetrics.cellXAt(menu.getGridData(), mouseX - gridLeft, CELL);
    }

    private int cellY(int mouseY) {
        return GridLayoutMetrics.cellYAt(menu.getGridData(), mouseY - gridTop, CELL);
    }

    private int cellX(int mouseX, int mouseY) {
        return GridLayoutMetrics.cellXAt(menu.getGridData(), mouseX - gridLeft, mouseY - gridTop, CELL);
    }

    private int cellY(int mouseX, int mouseY) {
        return GridLayoutMetrics.cellYAt(menu.getGridData(), mouseX - gridLeft, mouseY - gridTop, CELL);
    }

    private int anchorCellX(ItemStack stack) {
        int width = GridItemSizeManager.getSize(stack).placedWidth(rotatedPreview);
        return Math.max(0, Math.min(dragAnchorCellX, width - 1));
    }

    private int anchorCellY(ItemStack stack) {
        int height = GridItemSizeManager.getSize(stack).placedHeight(rotatedPreview);
        return Math.max(0, Math.min(dragAnchorCellY, height - 1));
    }

    private int targetGridX(ItemStack stack, int mouseX) {
        return cellX(mouseX, currentMouseY()) - anchorCellX(stack);
    }

    private int targetGridY(ItemStack stack, int mouseY) {
        return cellY(currentMouseX(), mouseY) - anchorCellY(stack);
    }

    private int targetRegionX(GridColumnPanel.Region region, ItemStack stack, int mouseX) {
        return region.cellX(mouseX, currentMouseY()) - anchorCellX(stack);
    }

    private int targetRegionY(GridColumnPanel.Region region, ItemStack stack, int mouseY) {
        return region.cellY(currentMouseX(), mouseY) - anchorCellY(stack);
    }

    private void setGridDragAnchor(int mouseX, int mouseY, int itemLeft, int itemTop, int width, int height) {
        int relativeX = Math.max(0, Math.min(width * CELL - 1, mouseX - itemLeft));
        int relativeY = Math.max(0, Math.min(height * CELL - 1, mouseY - itemTop));
        dragAnchorCellX = relativeX / CELL;
        dragAnchorCellY = relativeY / CELL;
        dragAnchorPixelX = relativeX % CELL;
        dragAnchorPixelY = relativeY % CELL;
    }

    private void setSlotDragAnchor(int mouseX, int mouseY, Slot slot) {
        dragAnchorCellX = 0;
        dragAnchorCellY = 0;
        if (menu.isPlayerGrid()) {
            Optional<FreeSlotWidget> customSlot = equipmentColumnPanel.slotAt(mouseX, mouseY);
            if (customSlot.isPresent()) {
                FreeSlotWidget widget = customSlot.get();
                dragAnchorPixelX = Math.max(0, Math.min(CELL - 1, (mouseX - widget.x()) * CELL / widget.size()));
                dragAnchorPixelY = Math.max(0, Math.min(CELL - 1, (mouseY - widget.y()) * CELL / widget.size()));
                return;
            }
        }
        dragAnchorPixelX = Math.max(0, Math.min(CELL - 1, mouseX - leftPos - slot.x));
        dragAnchorPixelY = Math.max(0, Math.min(CELL - 1, mouseY - topPos - slot.y));
    }

    private Slot findHoveredSlot(double mouseX, double mouseY) {
        if (menu.isPlayerGrid()) {
            return equipmentColumnPanel.slotAt(mouseX, mouseY)
                    .map(slot -> menu.slots.get(slot.menuIndex()))
                    .orElse(null);
        }
        for (Slot slot : menu.slots) {
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    private void clearDragState() {
        draggingEntry = null;
        draggingEquipmentEntry = null;
        draggingCurioSlot = null;
        dragPreviewStack = ItemStack.EMPTY;
        selectedPlayerStack = ItemStack.EMPTY;
        lastPlayerSlot = -1;
        rotatedPreview = false;
        dragAnchorCellX = 0;
        dragAnchorCellY = 0;
        dragAnchorPixelX = CELL / 2;
        dragAnchorPixelY = CELL / 2;
    }
}
