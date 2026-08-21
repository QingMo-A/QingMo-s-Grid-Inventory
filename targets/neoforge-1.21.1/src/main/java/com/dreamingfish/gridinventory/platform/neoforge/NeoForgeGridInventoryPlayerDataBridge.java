package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.target.neoforge1211.registry.NeoForge1211Attachments;
import net.minecraft.world.entity.player.Player;

public final class NeoForgeGridInventoryPlayerDataBridge implements GridInventoryPlayerDataBridge {
    @Override
    public GridInventoryData getPlayerGridInventory(Player player) {
        return player.getData(NeoForge1211Attachments.PLAYER_GRID_INVENTORY);
    }

    @Override
    public void setPlayerGridInventory(Player player, GridInventoryData data) {
        player.setData(NeoForge1211Attachments.PLAYER_GRID_INVENTORY, data);
        player.getInventory().setChanged();
    }

    @Override
    public GridInventoryData copyPlayerGridInventory(Player player) {
        return getPlayerGridInventory(player).copy();
    }
}
