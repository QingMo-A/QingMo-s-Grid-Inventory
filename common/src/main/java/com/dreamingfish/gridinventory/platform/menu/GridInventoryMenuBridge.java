package com.dreamingfish.gridinventory.platform.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public interface GridInventoryMenuBridge {
    default void openPlayerGridInventory(ServerPlayer player, GridInventoryData data) {
        openGridInventory(player, -1, InteractionHand.MAIN_HAND, true, data);
    }

    void openGridInventory(ServerPlayer player, int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data);
}
