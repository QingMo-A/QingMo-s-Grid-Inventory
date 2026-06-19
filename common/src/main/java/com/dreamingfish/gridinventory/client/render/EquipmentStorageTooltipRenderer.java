package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.client.ui.GridUiLayers;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class EquipmentStorageTooltipRenderer {
    private static final long CACHE_TTL_NANOS = 250_000_000L;
    private static final int CELL = 27;
    private static final int GRID_BORDER = 1;
    private static final int PADDING = 8;
    private static final int CONTAINER_GAP = 10;
    private static final int TITLE_HEIGHT = 12;
    private static CachedTooltip cachedTooltip;

    private EquipmentStorageTooltipRenderer() {
    }

    public static void render(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        CachedTooltip tooltip = tooltip(stack);
        if (tooltip == null) {
            return;
        }
        int panelWidth = tooltip.panelWidth();
        int panelHeight = tooltip.panelHeight();
        int panelX = mouseX - panelWidth - 14;
        if (panelX < 4) {
            panelX = Math.min(screenWidth - panelWidth - 4, mouseX + 18);
        }
        int panelY = Mth.clamp(mouseY - panelHeight / 2, 4, Math.max(4, screenHeight - panelHeight - 4));

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, GridUiLayers.EQUIPMENT_TOOLTIP);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0181818);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xFFD0D0D0);

        int y = panelY + PADDING;
        for (ContainerView container : tooltip.containers()) {
            if (tooltip.showSectionTitles()) {
                graphics.drawString(Minecraft.getInstance().font, container.title(),
                        panelX + PADDING, y, 0x94A3B8, false);
                y += TITLE_HEIGHT;
            }
            int gridLeft = panelX + PADDING + GRID_BORDER;
            int gridTop = y + GRID_BORDER;
            GridRenderer.renderGrid(graphics, gridLeft, gridTop, container.inventory(), CELL);
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F,
                    GridUiLayers.EQUIPMENT_TOOLTIP_ITEM - GridUiLayers.EQUIPMENT_TOOLTIP);
            for (var entry : container.inventory().getEntries()) {
                GridItemRenderer.renderEntry(graphics, entry, container.inventory(), gridLeft, gridTop, CELL, 1.0F);
            }
            graphics.pose().popPose();
            y += container.gridOuterHeight() + CONTAINER_GAP;
        }
        graphics.pose().popPose();
        graphics.flush();
    }

    private static CachedTooltip tooltip(ItemStack stack) {
        long now = System.nanoTime();
        if (cachedTooltip != null
                && now - cachedTooltip.createdNanos() <= CACHE_TTL_NANOS
                && ItemStack.matches(cachedTooltip.stack(), stack)) {
            return cachedTooltip;
        }
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        if (storage == null || storage.containers().isEmpty()) {
            storage = EquipmentStorageManager.initializeStorage(stack, GridEquipmentSlots.back());
        }
        if (storage == null || storage.containers().isEmpty()) {
            cachedTooltip = null;
            return null;
        }
        int contentWidth = 0;
        int contentHeight = 0;
        boolean showSectionTitles = storage.containers().size() > 1;
        List<ContainerView> containers = storage.containers().stream()
                .map(container -> new ContainerView(Component.literal(container.title()), container.inventory(),
                        gridOuterWidth(container), gridOuterHeight(container)))
                .toList();
        for (ContainerView container : containers) {
            contentWidth = Math.max(contentWidth, container.gridOuterWidth());
            if (showSectionTitles) {
                contentHeight += TITLE_HEIGHT;
            }
            contentHeight += container.gridOuterHeight() + CONTAINER_GAP;
        }
        cachedTooltip = new CachedTooltip(stack.copy(), containers, contentWidth + PADDING * 2,
                contentHeight + PADDING * 2 - CONTAINER_GAP, showSectionTitles, now);
        return cachedTooltip;
    }

    private static int gridOuterWidth(NamedGridInventoryData container) {
        return GridLayoutMetrics.width(container.inventory(), CELL) + GRID_BORDER * 2;
    }

    private static int gridOuterHeight(NamedGridInventoryData container) {
        return GridLayoutMetrics.height(container.inventory(), CELL) + GRID_BORDER * 2;
    }

    private record CachedTooltip(ItemStack stack, List<ContainerView> containers, int panelWidth, int panelHeight,
                                 boolean showSectionTitles, long createdNanos) {
    }

    private record ContainerView(Component title, com.dreamingfish.gridinventory.common.data.GridInventoryData inventory,
                                 int gridOuterWidth, int gridOuterHeight) {
    }
}
