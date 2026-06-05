package com.dreamingfish.gridinventory.fabric.client;

import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class FabricClientKeyMappings {
    private FabricClientKeyMappings() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.PICKUP_ITEM);
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.ROTATE_GRID_ITEM);
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.DROP_HOVERED_GRID_ITEM);
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.TOGGLE_BACKPACK_FOLD);
    }
}
