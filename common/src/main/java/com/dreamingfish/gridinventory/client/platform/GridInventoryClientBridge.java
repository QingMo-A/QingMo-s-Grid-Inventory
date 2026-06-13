package com.dreamingfish.gridinventory.client.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;

public interface GridInventoryClientBridge {
    GridInventoryClientBridge NOOP = (graphics, left, top, right, bottom, scale, mouseX, mouseY, entity) -> {
    };

    void renderEntityInInventoryFollowsMouse(GuiGraphics graphics, int left, int top, int right, int bottom,
                                             int scale, int mouseX, int mouseY, LivingEntity entity);
}
