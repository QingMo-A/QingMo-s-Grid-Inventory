package com.dreamingfish.gridinventory.target.forge1201.client;

import com.dreamingfish.gridinventory.platform.client.GridInventoryClientBridge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

public final class Forge1201ClientBridge implements GridInventoryClientBridge {
    @Override
    public void renderEntityInInventoryFollowsMouse(GuiGraphics graphics, int left, int top, int right, int bottom,
                                                    int scale, int mouseX, int mouseY, LivingEntity entity) {
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, (left + right) / 2, bottom - 4, scale,
                (left + right) / 2.0F - mouseX, (top + bottom) / 2.0F - mouseY, entity);
    }
}
