package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.entity.player.Player;

public interface GridInventoryPlayerDataBridge {
    GridInventoryData getPlayerGridInventory(Player player);

    void setPlayerGridInventory(Player player, GridInventoryData data);

    GridInventoryData copyPlayerGridInventory(Player player);
}
