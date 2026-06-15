package com.dreamingfish.gridinventory.client.screen.card;

import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.client.screen.panel.GridColumnPanel;
import com.dreamingfish.gridinventory.client.ui.animation.HoverAnimationTracker;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class StorageCardBodyRenderer {
    private static final int CELL = 27;

    private StorageCardBodyRenderer() {
    }

    public static int fullHeight(StorageCardData data) {
        if (!data.available() || data.containers().isEmpty()) {
            return 0;
        }
        int height = 10;
        boolean showSectionTitles = data.containers().size() > 1;
        for (NamedGridInventoryData container : data.containers()) {
            if (showSectionTitles) {
                height += 12;
            }
            height += GridLayoutMetrics.height(container.inventory(), CELL) + 10;
        }
        return height + 2;
    }

    public static void render(GuiGraphics graphics, StorageCardData data, int x, int y, int width, int visibleHeight,
                              float expansionProgress, int mouseX, int mouseY,
                              @Nullable UUID draggedPocketEntryId,
                              @Nullable GridColumnPanel.EquipmentEntryHit draggedEquipmentEntry,
                              HoverAnimationTracker<UUID> pocketHoverAnimations,
                              HoverAnimationTracker<String> equipmentHoverAnimations,
                              boolean hoverEnabled,
                              List<GridColumnPanel.Region> equipmentRegions) {
        if (visibleHeight <= 0 || data.containers().isEmpty()) {
            return;
        }
        graphics.fill(x, y, x + width, y + visibleHeight, 0xFF12171E);
        graphics.renderOutline(x, y, width, visibleHeight, 0x1FFFFFFF);
        graphics.enableScissor(x, y, x + width, y + visibleHeight);
        int cursorY = y + 8 - Math.round((1.0F - expansionProgress) * 4.0F);
        boolean showSectionTitles = data.containers().size() > 1;
        for (NamedGridInventoryData container : data.containers()) {
            if (showSectionTitles) {
                graphics.drawString(Minecraft.getInstance().font, Component.literal(container.title()),
                        x + 10, cursorY, 0x94A3B8, false);
                cursorY += 12;
            }
            int gridLeft = x + 10;
            int gridTop = cursorY;
            GridInventoryData inventory = container.inventory();
            if (data.slot() != null) {
                equipmentRegions.add(new GridColumnPanel.Region(data.slot(), container.id(), inventory, gridLeft, gridTop));
            }
            GridRenderer.renderGrid(graphics, gridLeft, gridTop, inventory, CELL);
            for (GridEntry entry : inventory.getEntries()) {
                renderEntry(graphics, data, container, inventory, entry, gridLeft, gridTop, mouseX, mouseY,
                        draggedPocketEntryId, draggedEquipmentEntry, pocketHoverAnimations, equipmentHoverAnimations,
                        hoverEnabled);
            }
            cursorY += GridLayoutMetrics.height(inventory, CELL) + 10;
        }
        graphics.disableScissor();
    }

    private static void renderEntry(GuiGraphics graphics, StorageCardData data, NamedGridInventoryData container,
                                    GridInventoryData inventory, GridEntry entry, int gridLeft, int gridTop,
                                    int mouseX, int mouseY, @Nullable UUID draggedPocketEntryId,
                                    @Nullable GridColumnPanel.EquipmentEntryHit draggedEquipmentEntry,
                                    HoverAnimationTracker<UUID> pocketHoverAnimations,
                                    HoverAnimationTracker<String> equipmentHoverAnimations, boolean hoverEnabled) {
        UUID draggedId = data.key() == StorageCardKey.POCKET ? draggedPocketEntryId
                : draggedEquipmentEntry != null
                && draggedEquipmentEntry.slot() == data.slot()
                && draggedEquipmentEntry.containerId().equals(container.id())
                ? draggedEquipmentEntry.entry().entryId() : null;
        boolean dragged = draggedId != null && draggedId.equals(entry.entryId());
        boolean hovered = hoverEnabled && !dragged
                && entry.contains(GridLayoutMetrics.cellXAt(inventory, mouseX - gridLeft, mouseY - gridTop, CELL),
                GridLayoutMetrics.cellYAt(inventory, mouseX - gridLeft, mouseY - gridTop, CELL));
        float hoverProgress;
        if (data.key() == StorageCardKey.POCKET) {
            hoverProgress = pocketHoverAnimations.update(entry.entryId(), hovered);
        } else {
            hoverProgress = equipmentHoverAnimations.update(data.slot().getName() + ":" + container.id() + ":" + entry.entryId(), hovered);
        }
        GridItemRenderer.renderEntry(graphics, entry, inventory, gridLeft, gridTop, CELL,
                dragged ? 0.35F : 1.0F, dragged ? 0.0F : hoverProgress);
    }
}
