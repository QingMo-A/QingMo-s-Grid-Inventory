package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class Forge1201MenuBridge implements GridInventoryMenuBridge {
    @Override
    public void openGridInventory(ServerPlayer player, int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data) {
        throw new UnsupportedOperationException("Forge 1.20.1 MenuBridge is not implemented yet");
    }
}
