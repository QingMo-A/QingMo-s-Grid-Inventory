package com.dreamingfish.gridinventory.client.render;

public final class ItemEntityHighlightRenderer {
    private ItemEntityHighlightRenderer() {
    }

    public static void render() {
        // Highlighting is routed through Minecraft#shouldEntityAppearGlowing by platform mixins,
        // so LevelRenderer can use the vanilla entity_outline post shader.
    }
}
