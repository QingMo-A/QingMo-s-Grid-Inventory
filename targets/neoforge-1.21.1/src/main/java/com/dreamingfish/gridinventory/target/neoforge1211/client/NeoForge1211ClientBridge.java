package com.dreamingfish.gridinventory.target.neoforge1211.client;

import com.dreamingfish.gridinventory.platform.client.GridInventoryClientBridge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;

public final class NeoForge1211ClientBridge implements GridInventoryClientBridge {
    @Override
    public void renderEntityInInventoryFollowsMouse(GuiGraphics graphics, int left, int top, int right, int bottom,
                                                    int scale, int mouseX, int mouseY, LivingEntity entity) {
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, left, top, right, bottom, scale, 0.0625F, mouseX, mouseY, entity);
    }
}
