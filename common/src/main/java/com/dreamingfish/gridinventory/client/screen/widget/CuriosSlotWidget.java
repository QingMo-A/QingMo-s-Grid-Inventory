package com.dreamingfish.gridinventory.client.screen.widget;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CuriosSlotWidget(CuriosSlotView view, int x, int y, int size) {
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + size && mouseY < y + size;
    }

    public void render(GuiGraphics graphics, boolean hovered, @Nullable Boolean dropAllowed, boolean dragged) {
        graphics.fill(x, y, x + size, y + size, hovered ? 0x6630343A : 0x33202020);
        int outline = 0xAA7A69A8;
        if (dropAllowed != null) {
            outline = dropAllowed ? 0xFF58DE86 : hovered ? 0xFFF06161 : 0xAA7A4242;
        } else if (hovered) {
            outline = 0xFFE3E8EE;
        }
        graphics.renderOutline(x, y, size, size, outline);
        if (view.stack().isEmpty()) {
            return;
        }
        renderItem(graphics, view.stack(), dragged ? 0.36F : 1.0F);
    }

    public List<Component> tooltip() {
        if (view.stack().isEmpty()) {
            return List.of(Component.literal(view.identifier()).withStyle(ChatFormatting.GRAY));
        }
        return List.of(view.stack().getHoverName(), Component.literal("Curios: " + view.identifier()).withStyle(ChatFormatting.GRAY));
    }

    private void renderItem(GuiGraphics graphics, ItemStack stack, float alpha) {
        int padding = GridInventoryServices.clientConfig().freeSlotItemPadding();
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
