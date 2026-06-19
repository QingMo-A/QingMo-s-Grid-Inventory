package com.dreamingfish.gridinventory.client.screen;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.render.EquipmentStorageTooltipRenderer;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerAccess;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.ExtractToPlayerInventoryMessage;
import com.dreamingfish.gridinventory.common.network.ExtractGridEntryToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.ExtractNestedGridEntryToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.InsertFromPlayerInventoryMessage;
import com.dreamingfish.gridinventory.common.network.MoveGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.InsertIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.InsertPlayerSlotIntoNestedGridMessage;
import com.dreamingfish.gridinventory.common.network.MoveEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.ExtractEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.TransferGridEntryIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryIntoGridMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryIntoNestedGridMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.TransferGridEntryIntoNestedGridMessage;
import com.dreamingfish.gridinventory.common.network.TransferNestedGridEntryIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.TransferNestedGridEntryIntoGridMessage;
import com.dreamingfish.gridinventory.common.network.TransferNestedGridEntryIntoNestedGridMessage;
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
import com.dreamingfish.gridinventory.common.network.ExtractCurioToNestedGridMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.DropNestedGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipNestedGridEntryMessage;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyGroundItemView;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import com.dreamingfish.gridinventory.client.screen.widget.NestedContainerWindowManager;
import com.dreamingfish.gridinventory.client.screen.panel.EquipmentColumnPanel;
import com.dreamingfish.gridinventory.client.screen.panel.GridColumnPanel;
import com.dreamingfish.gridinventory.client.screen.widget.FreeSlotWidget;
import com.dreamingfish.gridinventory.client.screen.widget.CuriosSlotWidget;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.access.SlotPositionAccessor;
import com.dreamingfish.gridinventory.client.sound.GridInventoryUiSounds;
import com.dreamingfish.gridinventory.client.ui.animation.HoverAnimationTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GridInventoryScreen extends AbstractContainerScreen<GridInventoryMenu> {
    private static final int CELL = 27;
    private static final int DRAG_START_DISTANCE = 6;
    private static final float DRAGGED_STACK_Z = 950.0F;
    private int gridLeft;
    private int gridTop;
    private GridEntry draggingEntry;
    private GridColumnPanel.EquipmentEntryHit draggingEquipmentEntry;
    private NestedContainerWindowManager.EntryHit draggingNestedEntry;
    private CuriosSlotWidget draggingCurioSlot;
    private GridEntry pendingGridDrag;
    private GridColumnPanel.EquipmentEntryHit pendingEquipmentDrag;
    private NestedContainerWindowManager.EntryHit pendingNestedDrag;
    private CuriosSlotWidget pendingCurioDrag;
    private Slot pendingSlotDrag;
    private int pendingSlotIndex = -1;
    private int pendingMouseX;
    private int pendingMouseY;
    private int pendingItemLeft;
    private int pendingItemTop;
    private int pendingItemWidth = 1;
    private int pendingItemHeight = 1;
    private ItemStack dragPreviewStack = ItemStack.EMPTY;
    private boolean rotatedPreview;
    private int lastPlayerSlot = -1;
    private ItemStack selectedPlayerStack = ItemStack.EMPTY;
    private int dragAnchorCellX;
    private int dragAnchorCellY;
    private int dragAnchorPixelX = CELL / 2;
    private int dragAnchorPixelY = CELL / 2;
    private final NearbyItemsPanel nearbyItemsPanel = new NearbyItemsPanel();
    private final NestedContainerWindowManager nestedWindows = new NestedContainerWindowManager();
    private final EquipmentColumnPanel equipmentColumnPanel = new EquipmentColumnPanel();
    private final GridColumnPanel gridColumnPanel = new GridColumnPanel();
    private final HoverAnimationTracker<UUID> gridHoverAnimations = new HoverAnimationTracker<>();
    private int workspaceMarginX;
    private int workspaceMarginY;
    private int workspacePaddingX;
    private int workspacePaddingY;
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
            workspaceMarginX = Math.max(12, Math.min(24, width / 34));
            workspaceMarginY = Math.max(8, Math.min(18, height / 32));
            workspacePaddingX = Math.max(14, Math.min(24, width / 48));
            workspacePaddingY = Math.max(10, Math.min(18, height / 54));
            columnGap = GridInventoryServices.clientConfig().columnGap();
            imageWidth = Math.max(360, width - workspaceMarginX * 2);
            imageHeight = Math.max(222, height - workspaceMarginY * 2);
        }
        super.init();
        if (menu.isPlayerGrid()) {
            int contentLeft = leftPos + workspacePaddingX;
            workspaceTop = topPos + workspacePaddingY;
            columnHeight = Math.max(184, imageHeight - workspacePaddingY * 2);
            int contentWidth = Math.max(320, imageWidth - workspacePaddingX * 2);
            int nearbyColumns = Math.max(2, Math.min(
                    GridInventoryServices.clientConfig().nearbyPanelColumns(),
                    (contentWidth - 204 - 88 - columnGap * 2 - NearbyItemsPanel.CHROME_WIDTH) / NearbyItemsPanel.CELL
            ));
            int nearbyWidth = nearbyColumns * NearbyItemsPanel.CELL + NearbyItemsPanel.CHROME_WIDTH;
            equipmentWidth = Math.max(204, Math.min(232, contentWidth - nearbyWidth - columnGap * 2 - 88));
            gridColumnWidth = contentWidth - equipmentWidth - nearbyWidth - columnGap * 2;
            int gridColumnLeft = contentLeft + equipmentWidth + columnGap;
            int nearbyLeft = gridColumnLeft + gridColumnWidth + columnGap;
            equipmentColumnPanel.setBounds(contentLeft, workspaceTop, equipmentWidth, columnHeight);
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
        boolean mouseBlockedByNestedWindow = nestedWindows.containsWindowAt(mouseX, mouseY);
        int interactionMouseX = mouseBlockedByNestedWindow ? Integer.MIN_VALUE / 2 : mouseX;
        int interactionMouseY = mouseBlockedByNestedWindow ? Integer.MIN_VALUE / 2 : mouseY;
        if (menu.isPlayerGrid()) {
            graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0x2E101010);
            equipmentColumnPanel.render(graphics, interactionMouseX, interactionMouseY, hotbarTop(), menu.slots, lastPlayerSlot,
                    draggedStack(), !draggedStack().isEmpty(), !nestedWindows.isDraggingWindow());
            gridColumnPanel.render(graphics, menu.getGridData(), draggingEntry == null ? null : draggingEntry.entryId(),
                    draggingEquipmentEntry, interactionMouseX, interactionMouseY, gridHoverAnimations, hoverAnimationsEnabled());
            gridLeft = gridColumnPanel.pocketLeft();
            gridTop = gridColumnPanel.pocketTop();
            renderHover(graphics, interactionMouseX, interactionMouseY);
            renderPreview(graphics, interactionMouseX, interactionMouseY);
            renderEquipmentPreview(graphics, interactionMouseX, interactionMouseY);
            gridHoverAnimations.markFrameEnd();
            return;
        }
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0x00202020);
        GridRenderer.renderGrid(graphics, gridLeft, gridTop, menu.getGridData(), CELL);
        for (GridEntry entry : menu.getGridData().getEntries()) {
            if (draggingEntry != null && draggingEntry.entryId().equals(entry.entryId())) {
                continue;
            }
            boolean hovered = hoverAnimationsEnabled() && !mouseBlockedByNestedWindow
                    && entry.contains(cellX(mouseX, mouseY), cellY(mouseX, mouseY));
            float hoverProgress = gridHoverAnimations.update(entry.entryId(), hovered);
            GridItemRenderer.renderEntry(graphics, entry, gridLeft, gridTop, CELL, 1.0F, hoverProgress);
        }
        renderDraggedOriginShadow(graphics);
        renderHover(graphics, interactionMouseX, interactionMouseY);
        renderPreview(graphics, interactionMouseX, interactionMouseY);
        gridHoverAnimations.markFrameEnd();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.isPlayerGrid()) {
            super.renderLabels(graphics, mouseX, mouseY);
        }
    }

    private void hideVanillaSlots() {
        for (int menuIndex = 0; menuIndex < menu.slots.size(); menuIndex++) {
            setSlotPosition(menuIndex, leftPos - 1000 - menuIndex * CELL, topPos - 1000);
        }
    }

    private int hotbarTop() {
        return workspaceTop + columnHeight - 24;
    }

    private void setSlotPosition(int menuIndex, int absoluteX, int absoluteY) {
        SlotPositionAccessor accessor = (SlotPositionAccessor) menu.slots.get(menuIndex);
        accessor.df_grid_inventory$setX(absoluteX - leftPos);
        accessor.df_grid_inventory$setY(absoluteY - topPos);
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        // Hover feedback is now animated at the item renderer level. Placement previews keep their own colors.
    }

    private boolean hoverAnimationsEnabled() {
        return draggingEntry == null
                && draggingEquipmentEntry == null
                && draggingNestedEntry == null
                && draggingCurioSlot == null
                && !nearbyItemsPanel.isDraggingGroundItem()
                && !hasPendingDrag()
                && !nestedWindows.isDraggingWindow()
                && selectedPlayerStack.isEmpty();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x1A000000);
        super.render(graphics, mouseX, mouseY, partialTick);
        boolean draggingWindow = nestedWindows.isDraggingWindow();
        boolean draggingAnyItem = !draggedStack().isEmpty();
        boolean mouseOverNestedWindow = nestedWindows.containsWindowAt(mouseX, mouseY);
        if (!draggingWindow && !draggingAnyItem && !mouseOverNestedWindow) {
            renderTooltip(graphics, mouseX, mouseY);
            hoveredGridEntry(mouseX, mouseY).ifPresent(entry ->
                    graphics.renderTooltip(font, gridEntryTooltip(entry), Optional.empty(), mouseX, mouseY));
            if (menu.isPlayerGrid() && draggingEntry == null && draggingEquipmentEntry == null
                    && draggingNestedEntry == null && draggingCurioSlot == null && selectedPlayerStack.isEmpty()) {
                equipmentColumnPanel.slotAt(mouseX, mouseY)
                        .map(slot -> menu.slots.get(slot.menuIndex()))
                        .filter(Slot::hasItem)
                        .ifPresent(slot -> graphics.renderTooltip(font, tooltipWithQuickEquipHint(slot.getItem()), Optional.empty(), mouseX, mouseY));
                equipmentColumnPanel.curioSlotAt(mouseX, mouseY)
                        .ifPresent(slot -> graphics.renderTooltip(font, slot.tooltip(), Optional.empty(), mouseX, mouseY));
            }
            if (draggingEntry == null && draggingEquipmentEntry == null && draggingNestedEntry == null
                    && selectedPlayerStack.isEmpty()) {
                hoveredStack(mouseX, mouseY).ifPresent(stack ->
                        EquipmentStorageTooltipRenderer.render(graphics, stack, mouseX, mouseY, width, height));
            }
        }
        nearbyItemsPanel.render(graphics, mouseX, mouseY, draggingAnyItem);
        nestedWindows.refresh(this::resolveNestedWindowStack);
        nestedWindows.render(graphics, mouseX, mouseY, hoverAnimationsEnabled(), nestedPlacementPreview());
        if (draggingAnyItem) {
            renderDraggedStackGhost(graphics, mouseX, mouseY);
            return;
        }
        renderDraggedStackGhost(graphics, mouseX, mouseY);
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggedStack();
        if (stack.isEmpty() || !inGrid(mouseX, mouseY)) {
            return;
        }
        int x = targetGridX(stack, mouseX);
        int y = targetGridY(stack, mouseY);
        boolean valid = GridPlacementValidator.canPlace(menu.getGridData(), stack, x, y, rotatedPreview,
                draggingEntry == null ? null : draggingEntry.entryId(), menu.isPlayerGrid() ? 0 : 1);
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
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, DRAGGED_STACK_Z);
        GridItemRenderer.renderStackInArea(graphics, stack, left, top, w * CELL, h * CELL, 0.75F, rotatedPreview);
        graphics.pose().popPose();
        graphics.flush();
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
                    movingWithinSameRegion ? draggingEquipmentEntry.entry().entryId() : null, 1);
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
                : draggingNestedEntry != null ? draggingNestedEntry.entry().stack()
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
        GridInventoryUiSounds.foldBackpack();
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

    private void renderPlacementPreview(GuiGraphics graphics, GridInventoryData inventory,
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

    private void renderGridAreaHighlight(GuiGraphics graphics, GridInventoryData inventory, int left, int top,
                                         int targetX, int targetY, int width, int height, int fillColor, int outlineColor) {
        boolean[][] occupied = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellX = targetX + x;
                int cellY = targetY + y;
                if (!inventory.isEnabledCell(cellX, cellY)) {
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

    private void renderOccupiedOuterOutline(GuiGraphics graphics, GridInventoryData inventory, int left, int top,
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

    private boolean isOccupied(boolean[][] occupied, int x, int y) {
        return y >= 0 && y < occupied.length && x >= 0 && x < occupied[y].length && occupied[y][x];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Optional<NestedContainerWindowManager.EntryHit> nestedEntryHit = nestedWindows.entryAt((int) mouseX, (int) mouseY);
        if (button == 1 && nestedEntryHit.isPresent() && menu.isPlayerGrid()) {
            NestedContainerWindowManager.EntryHit hit = nestedEntryHit.get();
            GridInventoryServices.network().sendToServer(new QuickEquipNestedGridEntryMessage(
                    hit.ownerPath(), hit.containerId(), hit.entry().entryId()));
            GridInventoryUiSounds.equip();
            return true;
        }
        if (button == 0 && nestedEntryHit.isPresent()) {
            NestedContainerWindowManager.EntryHit hit = nestedEntryHit.get();
            beginPendingNestedDrag(hit, (int) mouseX, (int) mouseY, hit.drawX(), hit.drawY());
            return true;
        }
        if (nestedWindows.mouseClicked(mouseX, mouseY, button, width, height)) {
            return true;
        }
        if (nestedWindows.containsWindowAt((int) mouseX, (int) mouseY)) {
            return true;
        }
        if (button == 1 && nearbyItemsPanel.isDraggingGroundItem()) {
            rotateDraggedPreview();
            return true;
        }
        if (nearbyItemsPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (menu.isPlayerGrid() && gridColumnPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1 && (draggingEntry != null || draggingEquipmentEntry != null || draggingNestedEntry != null
                || draggingCurioSlot != null || !selectedPlayerStack.isEmpty())) {
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
                    beginPendingCurioDrag(curioHit.get(), (int) mouseX, (int) mouseY);
                    return true;
                }
                return button == 0;
            }
            Optional<GridColumnPanel.EquipmentEntryHit> equipmentHit = gridColumnPanel.equipmentEntryAt((int) mouseX, (int) mouseY);
            if (equipmentHit.isPresent()) {
                if (button == 1) {
                    GridInventoryServices.network().sendToServer(new QuickEquipEquipmentStorageEntryMessage(
                            equipmentHit.get().slot(), equipmentHit.get().containerId(), equipmentHit.get().entry().entryId()));
                    GridInventoryUiSounds.equip();
                    return true;
                }
                if (button == 0) {
                    GridColumnPanel.EquipmentEntryHit hit = equipmentHit.get();
                    beginPendingEquipmentDrag(hit, (int) mouseX, (int) mouseY,
                            hit.region().drawX(hit.entry().x(), hit.entry().y()),
                            hit.region().drawY(hit.entry().x(), hit.entry().y()));
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
                    GridInventoryUiSounds.equip();
                } else if (hasShiftDown() && button == 0) {
                    GridInventoryServices.network().sendToServer(new ExtractToPlayerInventoryMessage(hit.get().entryId(), hit.get().stack().getCount()));
                    GridInventoryUiSounds.unequip();
                } else if (button == 0) {
                    GridEntry entry = hit.get();
                    beginPendingGridDrag(entry, (int) mouseX, (int) mouseY,
                            gridLeft + entry.x() * CELL, gridTop + entry.y() * CELL);
                } else {
                    return false;
                }
                return true;
            }
        }
        Slot hovered = findHoveredSlot(mouseX, mouseY);
        if (hovered != null) {
            lastPlayerSlot = hovered.getSlotIndex();
            if (hasShiftDown() && button == 0) {
                GridInventoryServices.network().sendToServer(new InsertFromPlayerInventoryMessage(lastPlayerSlot, 0, 0, false, true, false));
                selectedPlayerStack = ItemStack.EMPTY;
                return true;
            }
            if (button == 1 && hovered.hasItem() && menu.isPlayerGrid()) {
                GridInventoryServices.network().sendToServer(new QuickEquipPlayerSlotMessage(lastPlayerSlot));
                GridInventoryUiSounds.equip();
                return true;
            }
            if (button == 0 && hovered.hasItem()) {
                beginPendingSlotDrag(hovered, (int) mouseX, (int) mouseY);
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
                && (draggingNestedEntry != null || draggingCurioSlot != null || !selectedPlayerStack.isEmpty())) {
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
        Optional<NestedContainerWindowManager.EntryHit> nestedEntryHit = nestedWindows.entryAt(mouseX, mouseY);
        if (nestedEntryHit.isPresent()) {
            NestedContainerWindowManager.EntryHit hit = nestedEntryHit.get();
            GridInventoryServices.network().sendToServer(new DropNestedGridEntryMessage(
                    hit.ownerPath(), hit.containerId(), hit.entry().entryId()));
            return true;
        }
        if (nestedWindows.containsWindowAt(mouseX, mouseY)) {
            return true;
        }
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
        if (nestedWindows.mouseReleased(button)) {
            return true;
        }
        if (button == 0 && nearbyItemsPanel.isDraggingGroundItem()) {
            handleGroundItemRelease((int) mouseX, (int) mouseY);
            nearbyItemsPanel.clearDrag();
            return true;
        }
        if (button == 0 && hasPendingDrag()) {
            if (openPendingNestedWindow((int) mouseX, (int) mouseY)) {
                clearPendingDrag();
                return true;
            }
            clearPendingDrag();
            return true;
        }
        if (menu.isPlayerGrid()) {
            if (gridColumnPanel.mouseReleased(button) || equipmentColumnPanel.mouseReleased(button)) {
                return true;
            }
        }
        if (nearbyItemsPanel.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && draggingCurioSlot != null) {
            boolean released = false;
            boolean unequipped = true;
            Optional<NestedContainerWindowManager.GridHit> nestedTarget = nestedWindows.gridAt((int) mouseX, (int) mouseY);
            if (nestedTarget.isPresent()) {
                NestedContainerWindowManager.GridHit target = nestedTarget.get();
                GridInventoryServices.network().sendToServer(new ExtractCurioToNestedGridMessage(
                        draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(), target.ownerPath(),
                        target.containerId(), target.cellX() - anchorCellX(draggedStack()),
                        target.cellY() - anchorCellY(draggedStack()), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            }
            if (!released && nestedWindows.containsWindowAt((int) mouseX, (int) mouseY)) {
                clearDragState();
                return true;
            }
            Optional<GridColumnPanel.Region> equipmentRegion = !released && menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region region = equipmentRegion.get();
                GridInventoryServices.network().sendToServer(new ExtractCurioToEquipmentStorageMessage(
                        draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(), region.slot(), region.containerId(),
                        targetRegionX(region, draggedStack(), (int) mouseX),
                        targetRegionY(region, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new ExtractCurioToGridMessage(
                        draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(),
                        targetGridX(draggedStack(), (int) mouseX),
                        targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractCurioToPlayerSlotMessage(
                            draggingCurioSlot.view().identifier(), draggingCurioSlot.view().index(), hovered.getSlotIndex()));
                    released = true;
                }
            }
            playReleaseSound(released, false, unequipped);
            clearDragState();
            return true;
        }
        if (button == 0 && draggingEquipmentEntry != null) {
            boolean released = false;
            boolean equipped = false;
            boolean unequipped = false;
            Optional<NestedContainerWindowManager.GridHit> nestedTarget = nestedWindows.gridAt((int) mouseX, (int) mouseY);
            if (nestedTarget.isPresent()) {
                NestedContainerWindowManager.GridHit target = nestedTarget.get();
                GridInventoryServices.network().sendToServer(new TransferEquipmentStorageEntryIntoNestedGridMessage(
                        draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                        target.ownerPath(), target.containerId(),
                        target.cellX() - anchorCellX(draggedStack()),
                        target.cellY() - anchorCellY(draggedStack()), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                playReleaseSound(true, false, false);
                clearDragState();
                return true;
            }
            if (nestedWindows.containsWindowAt((int) mouseX, (int) mouseY)) {
                clearDragState();
                return true;
            }
            Optional<CuriosSlotWidget> curioTarget = menu.isPlayerGrid() ? equipmentColumnPanel.curioSlotAt(mouseX, mouseY) : Optional.empty();
            if (curioTarget.isPresent()) {
                GridInventoryServices.network().sendToServer(new InsertEquipmentStorageEntryIntoCurioMessage(
                        draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                        curioTarget.get().view().identifier(), curioTarget.get().view().index()));
                GridInventoryUiSounds.equip();
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
                    released = true;
                } else {
                    GridInventoryServices.network().sendToServer(new TransferEquipmentStorageEntryMessage(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            target.slot(), target.containerId(),
                            targetRegionX(target, draggedStack(), (int) mouseX),
                            targetRegionY(target, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                    released = true;
                }
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new TransferEquipmentStorageEntryIntoGridMessage(
                        draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                        targetGridX(draggedStack(), (int) mouseX),
                        targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
                unequipped = true;
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractEquipmentStorageEntryMessage(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            hovered.getSlotIndex(), draggingEquipmentEntry.entry().stack().getCount()));
                    released = true;
                    unequipped = true;
                }
            }
            playReleaseSound(released, equipped, unequipped);
            clearDragState();
            return true;
        }
        if (button == 0 && draggingNestedEntry != null) {
            boolean released = false;
            Optional<NestedContainerWindowManager.GridHit> nestedTarget = nestedWindows.gridAt((int) mouseX, (int) mouseY);
            if (nestedTarget.isPresent()) {
                NestedContainerWindowManager.GridHit target = nestedTarget.get();
                GridInventoryServices.network().sendToServer(new TransferNestedGridEntryIntoNestedGridMessage(
                        draggingNestedEntry.ownerPath(), draggingNestedEntry.containerId(), draggingNestedEntry.entry().entryId(),
                        target.ownerPath(), target.containerId(),
                        target.cellX() - anchorCellX(draggedStack()),
                        target.cellY() - anchorCellY(draggedStack()), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            } else if (nestedWindows.containsWindowAt((int) mouseX, (int) mouseY)) {
                clearDragState();
                return true;
            } else {
                Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid()
                        ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
                if (equipmentRegion.isPresent()) {
                    GridColumnPanel.Region target = equipmentRegion.get();
                    GridInventoryServices.network().sendToServer(new TransferNestedGridEntryIntoEquipmentStorageMessage(
                            draggingNestedEntry.ownerPath(), draggingNestedEntry.containerId(),
                            draggingNestedEntry.entry().entryId(), target.slot(), target.containerId(),
                            targetRegionX(target, draggedStack(), (int) mouseX),
                            targetRegionY(target, draggedStack(), (int) mouseY), rotatedPreview,
                            GridBackpackItem.isFolded(draggedStack())));
                    released = true;
                }
            }
            if (!released && inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new TransferNestedGridEntryIntoGridMessage(
                        draggingNestedEntry.ownerPath(), draggingNestedEntry.containerId(), draggingNestedEntry.entry().entryId(),
                        targetGridX(draggedStack(), (int) mouseX),
                        targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            }
            if (!released) {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractNestedGridEntryToPlayerSlotMessage(
                            draggingNestedEntry.ownerPath(), draggingNestedEntry.containerId(),
                            draggingNestedEntry.entry().entryId(), hovered.getSlotIndex(),
                            draggingNestedEntry.entry().stack().getCount()));
                    released = true;
                }
            }
            playReleaseSound(released, false, false);
            clearDragState();
            return true;
        }
        if (button == 0 && draggingEntry != null) {
            boolean released = false;
            boolean equipped = false;
            Optional<NestedContainerWindowManager.GridHit> nestedTarget = nestedWindows.gridAt((int) mouseX, (int) mouseY);
            if (nestedTarget.isPresent()) {
                NestedContainerWindowManager.GridHit target = nestedTarget.get();
                GridInventoryServices.network().sendToServer(new TransferGridEntryIntoNestedGridMessage(
                        draggingEntry.entryId(), target.ownerPath(), target.containerId(),
                        target.cellX() - anchorCellX(draggedStack()),
                        target.cellY() - anchorCellY(draggedStack()), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                playReleaseSound(true, false, false);
                clearDragState();
                return true;
            }
            if (nestedWindows.containsWindowAt((int) mouseX, (int) mouseY)) {
                clearDragState();
                return true;
            }
            Optional<CuriosSlotWidget> curioTarget = menu.isPlayerGrid() ? equipmentColumnPanel.curioSlotAt(mouseX, mouseY) : Optional.empty();
            if (curioTarget.isPresent()) {
                GridInventoryServices.network().sendToServer(new InsertGridEntryIntoCurioMessage(
                        draggingEntry.entryId(), curioTarget.get().view().identifier(), curioTarget.get().view().index()));
                GridInventoryUiSounds.equip();
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
                released = true;
                equipped = true;
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new MoveGridEntryMessage(draggingEntry.entryId(),
                        targetGridX(draggedStack(), (int) mouseX), targetGridY(draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new ExtractGridEntryToPlayerSlotMessage(draggingEntry.entryId(), hovered.getSlotIndex(), draggingEntry.stack().getCount()));
                    released = true;
                }
            }
            playReleaseSound(released, equipped, false);
            clearDragState();
            return true;
        }
        if (button == 0 && !selectedPlayerStack.isEmpty() && lastPlayerSlot >= 0) {
            boolean released = false;
            boolean equipped = false;
            Optional<NestedContainerWindowManager.GridHit> nestedTarget = nestedWindows.gridAt((int) mouseX, (int) mouseY);
            if (nestedTarget.isPresent()) {
                NestedContainerWindowManager.GridHit target = nestedTarget.get();
                GridInventoryServices.network().sendToServer(new InsertPlayerSlotIntoNestedGridMessage(
                        lastPlayerSlot, target.ownerPath(), target.containerId(),
                        target.cellX() - anchorCellX(draggedStack()),
                        target.cellY() - anchorCellY(draggedStack()), rotatedPreview,
                        GridBackpackItem.isFolded(draggedStack())));
                playReleaseSound(true, false, false);
                clearDragState();
                return true;
            }
            if (nestedWindows.containsWindowAt((int) mouseX, (int) mouseY)) {
                clearDragState();
                return true;
            }
            Optional<CuriosSlotWidget> curioTarget = menu.isPlayerGrid() ? equipmentColumnPanel.curioSlotAt(mouseX, mouseY) : Optional.empty();
            if (curioTarget.isPresent()) {
                GridInventoryServices.network().sendToServer(new InsertPlayerSlotIntoCurioMessage(
                        lastPlayerSlot, curioTarget.get().view().identifier(), curioTarget.get().view().index()));
                GridInventoryUiSounds.equip();
                clearDragState();
                return true;
            }
            Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region region = equipmentRegion.get();
                GridInventoryServices.network().sendToServer(new InsertIntoEquipmentStorageMessage(lastPlayerSlot, region.slot(), region.containerId(),
                        targetRegionX(region, draggedStack(), (int) mouseX),
                        targetRegionY(region, draggedStack(), (int) mouseY), rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
                equipped = true;
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                GridInventoryServices.network().sendToServer(new InsertFromPlayerInventoryMessage(lastPlayerSlot,
                        targetGridX(draggedStack(), (int) mouseX), targetGridY(draggedStack(), (int) mouseY), rotatedPreview, false, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null && hovered.getSlotIndex() != lastPlayerSlot) {
                    GridInventoryServices.network().sendToServer(new MovePlayerFreeSlotMessage(lastPlayerSlot, hovered.getSlotIndex()));
                    released = true;
                }
            }
            playReleaseSound(released, equipped, false);
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
        if (nestedWindows.mouseDragged(mouseX, mouseY, button, width, height)) {
            return true;
        }
        if (nearbyItemsPanel.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && activatePendingDragIfMoved((int) mouseX, (int) mouseY)) {
            return true;
        }
        if (menu.isPlayerGrid()) {
            if (gridColumnPanel.mouseDragged(mouseX, mouseY, button) || equipmentColumnPanel.mouseDragged(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return handleMouseScrolled(mouseX, mouseY, scrollY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        return handleMouseScrolled(mouseX, mouseY, scrollY);
    }

    private boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY) {
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
        return false;
    }

    private boolean openPendingNestedWindow(int mouseX, int mouseY) {
        ItemStack stack = ItemStack.EMPTY;
        if (pendingGridDrag != null) {
            stack = pendingGridDrag.stack();
            return nestedWindows.open(stack, NestedContainerPath.root().gridEntry(pendingGridDrag.entryId()), mouseX, mouseY, width, height);
        } else if (pendingEquipmentDrag != null) {
            stack = pendingEquipmentDrag.entry().stack();
            return nestedWindows.open(stack, NestedContainerPath.root().equipmentEntry(
                    pendingEquipmentDrag.slot(), pendingEquipmentDrag.containerId(), pendingEquipmentDrag.entry().entryId()),
                    mouseX, mouseY, width, height);
        } else if (pendingNestedDrag != null) {
            stack = pendingNestedDrag.entry().stack();
            return nestedWindows.open(stack, pendingNestedDrag.childPath(), mouseX, mouseY, width, height);
        } else if (pendingCurioDrag != null) {
            stack = pendingCurioDrag.view().stack();
            return nestedWindows.open(stack, NestedContainerPath.root().accessory(
                    pendingCurioDrag.view().identifier(), pendingCurioDrag.view().index()), mouseX, mouseY, width, height);
        } else if (pendingSlotDrag != null) {
            stack = pendingSlotDrag.getItem();
            return nestedWindows.open(stack, NestedContainerPath.root().playerSlot(pendingSlotIndex), mouseX, mouseY, width, height);
        }
        return nestedWindows.open(stack, NestedContainerPath.root(), mouseX, mouseY, width, height);
    }

    private Optional<ItemStack> resolveNestedWindowStack(NestedContainerPath path) {
        if (path.segments().isEmpty()) {
            return Optional.empty();
        }
        NestedContainerPath.Segment first = path.segments().get(0);
        Optional<ItemStack> root = Optional.empty();
        if (first instanceof NestedContainerPath.GridEntrySegment gridEntry) {
            root = menu.getGridData().getEntry(gridEntry.entryId()).map(entry -> entry.stack().copy());
        } else if (first instanceof NestedContainerPath.EquipmentEntrySegment equipmentEntry) {
            root = resolveEquipmentEntryStack(equipmentEntry.slot(), equipmentEntry.containerId(), equipmentEntry.entryId());
        } else if (first instanceof NestedContainerPath.PlayerSlotSegment playerSlot) {
            root = resolvePlayerSlotStack(playerSlot.slot());
        } else if (first instanceof NestedContainerPath.AccessorySegment accessory) {
            Minecraft minecraft = Minecraft.getInstance();
            root = minecraft.player == null
                    ? Optional.empty()
                    : GridInventoryServices.accessories().getAccessoryStack(minecraft.player, accessory.identifier(), accessory.index())
                    .map(ItemStack::copy);
        }
        if (root.isEmpty()) {
            return Optional.empty();
        }
        if (path.segments().size() == 1) {
            return root;
        }
        return NestedContainerAccess.resolve(root.get(), path.tail()).map(NestedContainerAccess.Handle::stack);
    }

    private Optional<ItemStack> resolveEquipmentEntryStack(net.minecraft.world.entity.EquipmentSlot slot, String containerId, UUID entryId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return Optional.empty();
        }
        ItemStack equipped = GridEquipmentSlots.isBack(slot)
                ? GridInventoryServices.accessories().getAccessoryStack(minecraft.player, "back", 0).orElse(ItemStack.EMPTY)
                : minecraft.player.getItemBySlot(slot);
        if (equipped.isEmpty()) {
            return Optional.empty();
        }
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(equipped);
        if (storage == null) {
            return Optional.empty();
        }
        return storage.containers().stream()
                .filter(container -> container.id().equals(containerId))
                .findFirst()
                .flatMap(container -> container.inventory().getEntry(entryId))
                .map(entry -> entry.stack().copy());
    }

    private Optional<ItemStack> resolvePlayerSlotStack(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || slot < 0 || slot >= minecraft.player.getInventory().getContainerSize()) {
            return Optional.empty();
        }
        ItemStack stack = minecraft.player.getInventory().getItem(slot);
        return stack.isEmpty() ? Optional.empty() : Optional.of(stack.copy());
    }

    private Optional<NestedContainerWindowManager.PlacementPreview> nestedPlacementPreview() {
        ItemStack stack = draggedStack();
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        Optional<NestedContainerPath> sourceOwnerPath = Optional.empty();
        Optional<String> sourceContainerId = Optional.empty();
        Optional<UUID> sourceEntryId = Optional.empty();
        if (draggingNestedEntry != null) {
            sourceOwnerPath = Optional.of(draggingNestedEntry.ownerPath());
            sourceContainerId = Optional.of(draggingNestedEntry.containerId());
            sourceEntryId = Optional.of(draggingNestedEntry.entry().entryId());
        }
        return Optional.of(new NestedContainerWindowManager.PlacementPreview(stack, rotatedPreview,
                anchorCellX(stack), anchorCellY(stack), sourceOwnerPath, sourceContainerId, sourceEntryId));
    }

    private Optional<GridEntry> entryAt(int mouseX, int mouseY) {
        if (menu.isPlayerGrid() && !gridColumnPanel.pocketBodyContains(mouseX, mouseY)) {
            return Optional.empty();
        }
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
        List<Component> tooltip = new java.util.ArrayList<>(getTooltipFromItem(minecraft, entry.stack()));
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
        return GridInventoryServices.accessories().canQuickEquip(minecraft.player, stack);
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

    private void beginPendingGridDrag(GridEntry entry, int mouseX, int mouseY, int itemLeft, int itemTop) {
        clearPendingDrag();
        pendingGridDrag = entry;
        pendingMouseX = mouseX;
        pendingMouseY = mouseY;
        pendingItemLeft = itemLeft;
        pendingItemTop = itemTop;
        pendingItemWidth = entry.width();
        pendingItemHeight = entry.height();
    }

    private void beginPendingEquipmentDrag(GridColumnPanel.EquipmentEntryHit hit, int mouseX, int mouseY, int itemLeft, int itemTop) {
        clearPendingDrag();
        pendingEquipmentDrag = hit;
        pendingMouseX = mouseX;
        pendingMouseY = mouseY;
        pendingItemLeft = itemLeft;
        pendingItemTop = itemTop;
        pendingItemWidth = hit.entry().width();
        pendingItemHeight = hit.entry().height();
    }

    private void beginPendingNestedDrag(NestedContainerWindowManager.EntryHit hit, int mouseX, int mouseY, int itemLeft, int itemTop) {
        clearPendingDrag();
        pendingNestedDrag = hit;
        pendingMouseX = mouseX;
        pendingMouseY = mouseY;
        pendingItemLeft = itemLeft;
        pendingItemTop = itemTop;
        pendingItemWidth = hit.entry().width();
        pendingItemHeight = hit.entry().height();
    }

    private void beginPendingCurioDrag(CuriosSlotWidget slot, int mouseX, int mouseY) {
        clearPendingDrag();
        pendingCurioDrag = slot;
        pendingMouseX = mouseX;
        pendingMouseY = mouseY;
    }

    private void beginPendingSlotDrag(Slot slot, int mouseX, int mouseY) {
        clearPendingDrag();
        pendingSlotDrag = slot;
        pendingSlotIndex = slot.getSlotIndex();
        pendingMouseX = mouseX;
        pendingMouseY = mouseY;
    }

    private boolean activatePendingDragIfMoved(int mouseX, int mouseY) {
        if (!hasPendingDrag()) {
            return false;
        }
        int dx = mouseX - pendingMouseX;
        int dy = mouseY - pendingMouseY;
        if (dx * dx + dy * dy < DRAG_START_DISTANCE * DRAG_START_DISTANCE) {
            return true;
        }
        if (pendingGridDrag != null) {
            draggingEntry = pendingGridDrag;
            dragPreviewStack = draggingEntry.stack().copy();
            rotatedPreview = draggingEntry.rotated();
            setGridDragAnchor(pendingMouseX, pendingMouseY, pendingItemLeft, pendingItemTop, pendingItemWidth, pendingItemHeight);
        } else if (pendingEquipmentDrag != null) {
            draggingEquipmentEntry = pendingEquipmentDrag;
            dragPreviewStack = draggingEquipmentEntry.entry().stack().copy();
            rotatedPreview = draggingEquipmentEntry.entry().rotated();
            setGridDragAnchor(pendingMouseX, pendingMouseY, pendingItemLeft, pendingItemTop, pendingItemWidth, pendingItemHeight);
        } else if (pendingNestedDrag != null) {
            draggingNestedEntry = pendingNestedDrag;
            dragPreviewStack = draggingNestedEntry.entry().stack().copy();
            rotatedPreview = draggingNestedEntry.entry().rotated();
            setGridDragAnchor(pendingMouseX, pendingMouseY, pendingItemLeft, pendingItemTop, pendingItemWidth, pendingItemHeight);
        } else if (pendingCurioDrag != null) {
            draggingCurioSlot = pendingCurioDrag;
            selectedPlayerStack = ItemStack.EMPTY;
            lastPlayerSlot = -1;
            rotatedPreview = false;
            dragAnchorCellX = 0;
            dragAnchorCellY = 0;
            dragAnchorPixelX = CELL / 2;
            dragAnchorPixelY = CELL / 2;
        } else if (pendingSlotDrag != null) {
            selectedPlayerStack = pendingSlotDrag.getItem().copy();
            lastPlayerSlot = pendingSlotIndex;
            rotatedPreview = false;
            setSlotDragAnchor(pendingMouseX, pendingMouseY, pendingSlotDrag);
        }
        GridInventoryUiSounds.dragStart();
        clearPendingDrag();
        return true;
    }

    private boolean hasPendingDrag() {
        return pendingGridDrag != null || pendingEquipmentDrag != null || pendingNestedDrag != null || pendingCurioDrag != null || pendingSlotDrag != null;
    }

    private void playReleaseSound(boolean released, boolean equipped, boolean unequipped) {
        if (!released) {
            return;
        }
        if (equipped) {
            GridInventoryUiSounds.equip();
        } else if (unequipped) {
            GridInventoryUiSounds.unequip();
        } else {
            GridInventoryUiSounds.dragRelease();
        }
    }

    private void clearPendingDrag() {
        pendingGridDrag = null;
        pendingEquipmentDrag = null;
        pendingNestedDrag = null;
        pendingCurioDrag = null;
        pendingSlotDrag = null;
        pendingSlotIndex = -1;
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
        clearPendingDrag();
        draggingEntry = null;
        draggingEquipmentEntry = null;
        draggingNestedEntry = null;
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
