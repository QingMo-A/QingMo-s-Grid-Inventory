package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class EquipmentStorageTooltipRenderer {
    private static final int CELL = 21;
    private static final int PADDING = 6;
    private static final int TITLE_HEIGHT = 13;
    private static final int CONTAINER_LABEL_HEIGHT = 11;
    private static final int CONTAINER_GAP = 6;

    private EquipmentStorageTooltipRenderer() {
    }

    public static void render(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        EquipmentStorageData storage = GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        if (storage == null || storage.containers().isEmpty()) {
            storage = EquipmentStorageManager.initializeStorage(stack, EquipmentSlot.BODY);
        }
        if (storage == null || storage.containers().isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        Component title = Component.translatable("tooltip.df_grid_inventory.equipment_storage");
        int contentWidth = font.width(title);
        int contentHeight = TITLE_HEIGHT;
        for (NamedGridInventoryData container : storage.containers()) {
            contentWidth = Math.max(contentWidth, Math.max(font.width(container.title()), GridLayoutMetrics.width(container.inventory(), CELL)));
            contentHeight += CONTAINER_LABEL_HEIGHT + GridLayoutMetrics.height(container.inventory(), CELL) + CONTAINER_GAP;
        }
        int panelWidth = contentWidth + PADDING * 2;
        int panelHeight = contentHeight + PADDING * 2 - CONTAINER_GAP;
        int panelX = mouseX - panelWidth - 14;
        if (panelX < 4) {
            panelX = Math.min(screenWidth - panelWidth - 4, mouseX + 18);
        }
        int panelY = Mth.clamp(mouseY - panelHeight / 2, 4, Math.max(4, screenHeight - panelHeight - 4));

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0181818);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xFFD0D0D0);
        graphics.drawString(font, title, panelX + PADDING, panelY + PADDING, 0xFFFFFF, false);

        int y = panelY + PADDING + TITLE_HEIGHT;
        for (NamedGridInventoryData container : storage.containers()) {
            graphics.drawString(font, container.title(), panelX + PADDING, y, 0xC8C8C8, false);
            y += CONTAINER_LABEL_HEIGHT;
            GridRenderer.renderGrid(graphics, panelX + PADDING, y, container.inventory(), CELL);
            for (var entry : container.inventory().getEntries()) {
                GridItemRenderer.renderEntry(graphics, entry, container.inventory(), panelX + PADDING, y, CELL, 1.0F);
            }
            y += GridLayoutMetrics.height(container.inventory(), CELL) + CONTAINER_GAP;
        }
        graphics.pose().popPose();
    }
}
