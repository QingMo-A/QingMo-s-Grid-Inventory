package com.dreamingfish.gridinventory.client.render;

import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.pickup.ClientItemTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class PickupPromptHud {
    private static float alpha;

    private PickupPromptHud() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean visible = minecraft.screen == null && ClientItemTargeting.currentTargetItemEntityId() != -1;
        float target = visible ? 1.0F : 0.0F;
        float speed = visible ? 0.18F : 0.12F;
        alpha += (target - alpha) * speed;
        if (alpha < 0.01F) {
            alpha = 0.0F;
        }
    }

    public static void render(GuiGraphics graphics) {
        if (alpha <= 0.0F) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) {
            return;
        }

        Font font = minecraft.font;
        Component text = Component.translatable(
                "hud.df_grid_inventory.pickup_prompt",
                ModKeyMappings.PICKUP_ITEM.getTranslatedKeyMessage()
        );
        int width = font.width(text);
        int x = (graphics.guiWidth() - width) / 2;
        int y = graphics.guiHeight() / 2 + 18;
        int backgroundAlpha = Math.round(110.0F * alpha);
        int textAlpha = Math.round(255.0F * alpha);

        graphics.fill(x - 6, y - 4, x + width + 6, y + 12, (backgroundAlpha << 24));
        graphics.drawString(font, text, x, y, (textAlpha << 24) | 0xFFFFFF, false);
    }
}
