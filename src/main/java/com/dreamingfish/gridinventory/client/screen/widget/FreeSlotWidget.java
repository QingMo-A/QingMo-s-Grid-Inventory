package com.dreamingfish.gridinventory.client.screen.widget;

import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record FreeSlotWidget(int menuIndex, int x, int y, int size) {
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + size && mouseY < y + size;
    }

    public void render(GuiGraphics graphics, Slot slot, boolean hovered, boolean dragged) {
        graphics.fill(x, y, x + size, y + size, hovered ? 0xFF30343A : 0xFF202020);
        graphics.renderOutline(x, y, size, size, hovered ? 0xFFE3E8EE : 0xFF4C4C4C);
        if (slot.hasItem()) {
            renderItem(graphics, slot.getItem(), dragged ? 0.36F : 1.0F);
        }
    }

    private void renderItem(GuiGraphics graphics, ItemStack stack, float alpha) {
        int padding = GridInventoryClientConfig.FREE_SLOT_ITEM_PADDING.get();
        int iconSize = Math.max(8, size - padding * 2);
        float scale = iconSize / 16.0F;
        float iconX = x + (size - iconSize) / 2.0F;
        float iconY = y + (size - iconSize) / 2.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(iconX, iconY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItem(stack, 0, 0);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + size - 17, y + size - 17);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
