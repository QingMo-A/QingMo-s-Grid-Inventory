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
    public static final KeyMapping ROTATE_GRID_ITEM = new KeyMapping(
            "key.df_grid_inventory.rotate_grid_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    public static final KeyMapping DROP_HOVERED_GRID_ITEM = new KeyMapping(
            "key.df_grid_inventory.drop_hovered_grid_item",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
    );
    public static final KeyMapping TOGGLE_BACKPACK_FOLD = new KeyMapping(
            "key.df_grid_inventory.toggle_backpack_fold",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );

    private ModKeyMappings() {
    }
}
