package com.dreamingfish.gridinventory.client.screen;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.client.render.EquipmentStorageTooltipRenderer;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.ExtractToPlayerInventoryPacket;
import com.dreamingfish.gridinventory.common.network.ExtractGridEntryToPlayerSlotPacket;
import com.dreamingfish.gridinventory.common.network.InsertFromPlayerInventoryPacket;
import com.dreamingfish.gridinventory.common.network.MoveGridEntryPacket;
import com.dreamingfish.gridinventory.common.network.InsertIntoEquipmentStoragePacket;
import com.dreamingfish.gridinventory.common.network.MoveEquipmentStorageEntryPacket;
import com.dreamingfish.gridinventory.common.network.ExtractEquipmentStorageEntryPacket;
import com.dreamingfish.gridinventory.common.network.TransferGridEntryIntoEquipmentStoragePacket;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryIntoGridPacket;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryPacket;
import com.dreamingfish.gridinventory.common.network.MovePlayerFreeSlotPacket;
import com.dreamingfish.gridinventory.common.network.QuickEquipGridEntryPacket;
import com.dreamingfish.gridinventory.common.network.QuickEquipEquipmentStorageEntryPacket;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import com.dreamingfish.gridinventory.client.screen.panel.EquipmentColumnPanel;
import com.dreamingfish.gridinventory.client.screen.panel.GridColumnPanel;
import com.dreamingfish.gridinventory.client.screen.widget.FreeSlotWidget;
import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.mixin.client.SlotAccessor;
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
    private GridColumnPanel.EquipmentEntryHit draggingEquipmentEntry;
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
            columnGap = GridInventoryClientConfig.COLUMN_GAP.get();
            imageWidth = Math.max(360, width - workspaceMargin * 2);
            imageHeight = Math.max(222, height - workspaceMargin * 2);
        }
        super.init();
        if (menu.isPlayerGrid()) {
            workspaceTop = topPos;
            columnHeight = imageHeight - 38;
            int nearbyColumns = Math.max(2, Math.min(
                    GridInventoryClientConfig.NEARBY_PANEL_COLUMNS.get(),
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
            graphics.fill(leftPos - 4, topPos - 4, leftPos + imageWidth + 4, topPos + imageHeight + 4, 0xE0101010);
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
        SlotAccessor accessor = (SlotAccessor) (Object) menu.slots.get(menuIndex);
        accessor.df_grid_inventory$setX(absoluteX - leftPos);
        accessor.df_grid_inventory$setY(absoluteY - topPos);
    }

    private void renderHover(GuiGraphics graphics, int mouseX, int mouseY) {
        if (draggingEntry != null || draggingEquipmentEntry != null || !selectedPlayerStack.isEmpty()) {
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
        hoveredGridEntry(mouseX, mouseY).ifPresent(entry -> graphics.renderTooltip(font, List.of(
                entry.stack().getHoverName(),
                Component.literal("Size: " + entry.width() + " x " + entry.height()).withStyle(ChatFormatting.GRAY),
                Component.literal("Rotatable: " + (GridItemSizeManager.getSize(entry.stack()).rotatable() ? "Yes" : "No")).withStyle(ChatFormatting.GRAY)
        ), Optional.empty(), mouseX, mouseY));
        if (menu.isPlayerGrid() && draggingEntry == null && draggingEquipmentEntry == null && selectedPlayerStack.isEmpty()) {
            equipmentColumnPanel.slotAt(mouseX, mouseY)
                    .map(slot -> menu.slots.get(slot.menuIndex()))
                    .filter(Slot::hasItem)
                    .ifPresent(slot -> graphics.renderTooltip(font, slot.getItem(), mouseX, mouseY));
        }
        if (draggingEntry == null && draggingEquipmentEntry == null && selectedPlayerStack.isEmpty()) {
            hoveredStack(mouseX, mouseY).ifPresent(stack ->
                    EquipmentStorageTooltipRenderer.render(graphics, stack, mouseX, mouseY, width, height));
        }
    }

    private void renderPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggingEntry != null ? draggingEntry.stack()
                : draggingEquipmentEntry != null ? draggingEquipmentEntry.entry().stack() : selectedPlayerStack;
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
        ItemStack stack = draggingEntry != null ? draggingEntry.stack()
                : draggingEquipmentEntry != null ? draggingEquipmentEntry.entry().stack() : selectedPlayerStack;
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
            int drawX = region.left() + x * CELL;
            int drawY = region.top() + y * CELL;
            graphics.fill(drawX, drawY, drawX + w * CELL, drawY + h * CELL, valid ? 0x6630C860 : 0x66D84040);
            renderCellOutlines(graphics, drawX, drawY, w, h, valid ? 0xCC7DFFA2 : 0xCCFF8888);
        });
    }

    private ItemStack draggedStack() {
        return draggingEntry != null ? draggingEntry.stack()
                : draggingEquipmentEntry != null ? draggingEquipmentEntry.entry().stack() : selectedPlayerStack;
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nearbyItemsPanel.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1 && (draggingEntry != null || draggingEquipmentEntry != null || !selectedPlayerStack.isEmpty())) {
            rotateDraggedPreview();
            return true;
        }
        if (menu.isPlayerGrid()) {
            Optional<GridColumnPanel.EquipmentEntryHit> equipmentHit = gridColumnPanel.equipmentEntryAt((int) mouseX, (int) mouseY);
            if (equipmentHit.isPresent()) {
                if (button == 1) {
                    PacketDistributor.sendToServer(new QuickEquipEquipmentStorageEntryPacket(
                            equipmentHit.get().slot(), equipmentHit.get().containerId(), equipmentHit.get().entry().entryId()));
                    return true;
                }
                if (button == 0) {
                    draggingEquipmentEntry = equipmentHit.get();
                    rotatedPreview = equipmentHit.get().entry().rotated();
                    setGridDragAnchor((int) mouseX, (int) mouseY,
                            draggingEquipmentEntry.region().left() + draggingEquipmentEntry.entry().x() * CELL,
                            draggingEquipmentEntry.region().top() + draggingEquipmentEntry.entry().y() * CELL,
                            draggingEquipmentEntry.entry().width(), draggingEquipmentEntry.entry().height());
                    return true;
                }
            }
        }
        if (inGrid((int) mouseX, (int) mouseY)) {
            int x = cellX((int) mouseX);
            int y = cellY((int) mouseY);
            Optional<GridEntry> hit = menu.getGridData().getEntries().stream().filter(entry -> entry.contains(x, y)).findFirst();
            if (hit.isPresent()) {
                if (button == 1 && menu.isPlayerGrid()) {
                    PacketDistributor.sendToServer(new QuickEquipGridEntryPacket(hit.get().entryId()));
                } else if (hasShiftDown() && button == 0) {
                    PacketDistributor.sendToServer(new ExtractToPlayerInventoryPacket(hit.get().entryId(), hit.get().stack().getCount()));
                } else if (button == 0) {
                    draggingEntry = hit.get();
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
            lastPlayerSlot = hovered.getSlotIndex();
            if (hasShiftDown() && button == 0) {
                PacketDistributor.sendToServer(new InsertFromPlayerInventoryPacket(lastPlayerSlot, 0, 0, false, true));
                selectedPlayerStack = ItemStack.EMPTY;
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (nearbyItemsPanel.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && draggingEquipmentEntry != null) {
            Optional<GridColumnPanel.Region> region = gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY);
            if (region.isPresent()) {
                GridColumnPanel.Region target = region.get();
                if (target.slot() == draggingEquipmentEntry.slot()
                        && target.containerId().equals(draggingEquipmentEntry.containerId())) {
                    PacketDistributor.sendToServer(new MoveEquipmentStorageEntryPacket(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            targetRegionX(target, draggingEquipmentEntry.entry().stack(), (int) mouseX),
                            targetRegionY(target, draggingEquipmentEntry.entry().stack(), (int) mouseY), rotatedPreview));
                } else {
                    PacketDistributor.sendToServer(new TransferEquipmentStorageEntryPacket(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            target.slot(), target.containerId(),
                            targetRegionX(target, draggingEquipmentEntry.entry().stack(), (int) mouseX),
                            targetRegionY(target, draggingEquipmentEntry.entry().stack(), (int) mouseY), rotatedPreview));
                }
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                PacketDistributor.sendToServer(new TransferEquipmentStorageEntryIntoGridPacket(
                        draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                        targetGridX(draggingEquipmentEntry.entry().stack(), (int) mouseX),
                        targetGridY(draggingEquipmentEntry.entry().stack(), (int) mouseY), rotatedPreview));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    PacketDistributor.sendToServer(new ExtractEquipmentStorageEntryPacket(
                            draggingEquipmentEntry.slot(), draggingEquipmentEntry.containerId(), draggingEquipmentEntry.entry().entryId(),
                            hovered.getSlotIndex(), draggingEquipmentEntry.entry().stack().getCount()));
                }
            }
            clearDragState();
            return true;
        }
        if (button == 0 && draggingEntry != null) {
            Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region target = equipmentRegion.get();
                PacketDistributor.sendToServer(new TransferGridEntryIntoEquipmentStoragePacket(
                        draggingEntry.entryId(), target.slot(), target.containerId(),
                        targetRegionX(target, draggingEntry.stack(), (int) mouseX),
                        targetRegionY(target, draggingEntry.stack(), (int) mouseY), rotatedPreview));
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                PacketDistributor.sendToServer(new MoveGridEntryPacket(draggingEntry.entryId(),
                        targetGridX(draggingEntry.stack(), (int) mouseX), targetGridY(draggingEntry.stack(), (int) mouseY), rotatedPreview));
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
            Optional<GridColumnPanel.Region> equipmentRegion = menu.isPlayerGrid() ? gridColumnPanel.equipmentRegionAt((int) mouseX, (int) mouseY) : Optional.empty();
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region region = equipmentRegion.get();
                PacketDistributor.sendToServer(new InsertIntoEquipmentStoragePacket(lastPlayerSlot, region.slot(), region.containerId(),
                        targetRegionX(region, selectedPlayerStack, (int) mouseX),
                        targetRegionY(region, selectedPlayerStack, (int) mouseY), rotatedPreview));
            } else if (inGrid((int) mouseX, (int) mouseY)) {
                PacketDistributor.sendToServer(new InsertFromPlayerInventoryPacket(lastPlayerSlot,
                        targetGridX(selectedPlayerStack, (int) mouseX), targetGridY(selectedPlayerStack, (int) mouseY), rotatedPreview, false));
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null && hovered.getSlotIndex() != lastPlayerSlot) {
                    PacketDistributor.sendToServer(new MovePlayerFreeSlotPacket(lastPlayerSlot, hovered.getSlotIndex()));
                }
            }
            clearDragState();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        int x = cellX(mouseX);
        int y = cellY(mouseY);
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
        }
        return hoveredGridEntry(mouseX, mouseY).map(GridEntry::stack);
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

    private int anchorCellX(ItemStack stack) {
        int width = GridItemSizeManager.getSize(stack).placedWidth(rotatedPreview);
        return Math.max(0, Math.min(dragAnchorCellX, width - 1));
    }

    private int anchorCellY(ItemStack stack) {
        int height = GridItemSizeManager.getSize(stack).placedHeight(rotatedPreview);
        return Math.max(0, Math.min(dragAnchorCellY, height - 1));
    }

    private int targetGridX(ItemStack stack, int mouseX) {
        return cellX(mouseX) - anchorCellX(stack);
    }

    private int targetGridY(ItemStack stack, int mouseY) {
        return cellY(mouseY) - anchorCellY(stack);
    }

    private int targetRegionX(GridColumnPanel.Region region, ItemStack stack, int mouseX) {
        return region.cellX(mouseX) - anchorCellX(stack);
    }

    private int targetRegionY(GridColumnPanel.Region region, ItemStack stack, int mouseY) {
        return region.cellY(mouseY) - anchorCellY(stack);
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
        selectedPlayerStack = ItemStack.EMPTY;
        lastPlayerSlot = -1;
        rotatedPreview = false;
        dragAnchorCellX = 0;
        dragAnchorCellY = 0;
        dragAnchorPixelX = CELL / 2;
        dragAnchorPixelY = CELL / 2;
    }
}
