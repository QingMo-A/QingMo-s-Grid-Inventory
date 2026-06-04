package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.registry.ModAttachments;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import net.minecraft.world.entity.player.Player;

public final class NeoForgeGridInventoryPlayerDataBridge implements GridInventoryPlayerDataBridge {
    @Override
    public GridInventoryData getPlayerGridInventory(Player player) {
        return player.getData(ModAttachments.PLAYER_GRID_INVENTORY);
    }

    @Override
    public void setPlayerGridInventory(Player player, GridInventoryData data) {
        player.setData(ModAttachments.PLAYER_GRID_INVENTORY, data);
    }

    @Override
    public GridInventoryData copyPlayerGridInventory(Player player) {
        return getPlayerGridInventory(player).copy();
    }
}
