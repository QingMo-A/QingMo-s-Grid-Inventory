package com.dreamingfish.gridinventory.client.render;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class ItemEntityHighlightRenderer {
    private ItemEntityHighlightRenderer() {
    }

    public static void render(RenderLevelStageEvent event) {
        // Highlighting is routed through Minecraft#shouldEntityAppearGlowing by MinecraftMixin,
        // so LevelRenderer can use the vanilla entity_outline post shader.
    }
}
