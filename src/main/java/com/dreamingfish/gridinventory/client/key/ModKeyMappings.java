package com.dreamingfish.gridinventory.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {
    public static final String CATEGORY = "key.categories.df_grid_inventory";
    public static final KeyMapping PICKUP_ITEM = new KeyMapping(
            "key.df_grid_inventory.pickup_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    private ModKeyMappings() {
    }
}
