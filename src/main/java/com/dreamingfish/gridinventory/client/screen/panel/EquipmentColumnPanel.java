package com.dreamingfish.gridinventory.client.screen.panel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public final class EquipmentColumnPanel {
    private static final int CELL = 18;
    private static final int ARMOR_FRAME = 30;
    private int left;
    private int top;
    private int width;
    private int height;

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int hotbarLeft, int hotbarTop) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        graphics.fill(left, top, left + width, top + height, 0xD91A1A1A);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.equipment"), left + 8, top + 7, 0xFFFFFF, false);

        int pairLeft = left + (width - ARMOR_FRAME * 2 - 30) / 2;
        int upperY = top + 27;
        int lowerY = hotbarTop - ARMOR_FRAME - 16;
        renderArmorPair(graphics, pairLeft, upperY, Component.translatable("screen.df_grid_inventory.armor_upper"));
        renderArmorPair(graphics, pairLeft, lowerY, Component.translatable("screen.df_grid_inventory.armor_lower"));

        int modelLeft = left + 7;
        int modelRight = left + width - 7;
        int modelTop = upperY + ARMOR_FRAME + 3;
        int modelBottom = lowerY - 3;
        int modelHeight = Math.max(1, modelBottom - modelTop);
        int scale = Math.min(52, Math.max(30, modelHeight / 2 + 8));
        graphics.enableScissor(modelLeft, modelTop, modelRight, modelBottom);
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, modelLeft, modelTop, modelRight, modelBottom, scale,
                0.0625F, mouseX, mouseY, minecraft.player
        );
        graphics.disableScissor();

        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.offhand"), left + 8, hotbarTop - 14, 0xBFBFBF, false);
        graphics.fill(left + 7, hotbarTop - 3, left + 29, hotbarTop + 19, 0xFF202020);
        graphics.renderOutline(left + 7, hotbarTop - 3, 22, 22, 0xFF4C4C4C);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.hotbar"), hotbarLeft, hotbarTop - 14, 0xBFBFBF, false);
        graphics.fill(hotbarLeft - 4, hotbarTop - 3, hotbarLeft + 9 * CELL + 4, hotbarTop + 19, 0xFF202020);
        graphics.renderOutline(hotbarLeft - 4, hotbarTop - 3, 9 * CELL + 8, 22, 0xFF383838);
    }

    private void renderArmorPair(GuiGraphics graphics, int pairLeft, int y, Component title) {
        graphics.drawString(Minecraft.getInstance().font, title, pairLeft, y - 12, 0x8F8F8F, false);
        renderArmorFrame(graphics, pairLeft, y);
        renderArmorFrame(graphics, pairLeft + ARMOR_FRAME + 30, y);
    }

    private void renderArmorFrame(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + ARMOR_FRAME, y + ARMOR_FRAME, 0xFF202020);
        graphics.renderOutline(x, y, ARMOR_FRAME, ARMOR_FRAME, 0xFF4C4C4C);
    }

}
