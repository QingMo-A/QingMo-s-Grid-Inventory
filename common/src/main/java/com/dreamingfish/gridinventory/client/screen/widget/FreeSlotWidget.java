package com.dreamingfish.gridinventory.client.screen.widget;

import com.dreamingfish.gridinventory.client.compat.rarity.GridItemRarityServices;
import com.dreamingfish.gridinventory.client.ui.GridUiMotion;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record FreeSlotWidget(int menuIndex, int x, int y, int size) {
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + size && mouseY < y + size;
    }

    public void render(GuiGraphics graphics, Slot slot, boolean hovered, boolean dragged, @Nullable Boolean dropAllowed, float hoverProgress) {
        float visualHover = dragged ? 0.0F : GridUiMotion.clamp(hoverProgress);
        GridUiMotion.renderShadow(graphics, x, y, size, size, visualHover);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -GridUiMotion.lift(visualHover), visualHover > 0.0F ? 8.0F : 0.0F);
        graphics.fill(x, y, x + size, y + size,
                GridUiMotion.lerpArgb(GridUiMotion.NORMAL_SLOT_BACKGROUND, GridUiMotion.HOVER_SLOT_BACKGROUND, visualHover));
        int outline = GridUiMotion.lerpArgb(0xFF4C4C4C, GridUiMotion.HOVER_SLOT_OUTLINE, visualHover);
        if (dropAllowed != null) {
            outline = dropAllowed ? 0xFF58DE86 : hovered ? 0xFFF06161 : 0xAA7A4242;
        }
        if (slot.hasItem()) {
            renderItem(graphics, slot.getItem(), dragged ? 0.36F : 1.0F, visualHover);
        }
        graphics.renderOutline(x, y, size, size, outline);
        graphics.pose().popPose();
    }

    private void renderItem(GuiGraphics graphics, ItemStack stack, float alpha, float hoverProgress) {
        GridItemRarityServices.compat().renderBackground(graphics, stack, x + 1, y + 1, size - 2, size - 2, alpha);
        int padding = GridInventoryServices.clientConfig().freeSlotItemPadding();
        int iconSize = Math.max(8, size - padding * 2);
        float scale = iconSize / 16.0F * GridUiMotion.iconScale(hoverProgress);
        float iconX = x + size / 2.0F;
        float iconY = y + size / 2.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(iconX, iconY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.renderItem(stack, -8, -8);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        GridItemRarityServices.compat().renderItemDecorations(graphics, Minecraft.getInstance().font, stack, x + size - 17, y + size - 17);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
