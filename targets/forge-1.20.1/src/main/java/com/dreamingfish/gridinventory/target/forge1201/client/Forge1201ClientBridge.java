package com.dreamingfish.gridinventory.target.forge1201.client;

import com.dreamingfish.gridinventory.client.platform.GridInventoryClientBridge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

public final class Forge1201ClientBridge implements GridInventoryClientBridge {
    @Override
    public void renderEntityInInventoryFollowsMouse(GuiGraphics graphics, int left, int top, int right, int bottom,
                                                    int scale, int mouseX, int mouseY, LivingEntity entity) {
        int modelHeight = Math.max(1, bottom - top);
        int anchorY = top + Math.round(modelHeight * 0.82F);
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, (left + right) / 2, anchorY, scale,
                (left + right) / 2.0F - mouseX, (top + bottom) / 2.0F - mouseY, entity);
    }
}
