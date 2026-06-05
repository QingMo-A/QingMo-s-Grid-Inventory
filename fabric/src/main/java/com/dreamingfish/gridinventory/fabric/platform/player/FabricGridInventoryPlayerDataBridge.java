package com.dreamingfish.gridinventory.fabric.platform.player;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.entity.player.Player;

public final class FabricGridInventoryPlayerDataBridge implements GridInventoryPlayerDataBridge {
    @Override
    public GridInventoryData getPlayerGridInventory(Player player) {
        if (player instanceof FabricGridInventoryDataHolder holder) {
            return holder.df_grid_inventory$getGridInventoryData();
        }
        return createDefault();
    }

    @Override
    public void setPlayerGridInventory(Player player, GridInventoryData data) {
        if (player instanceof FabricGridInventoryDataHolder holder) {
            holder.df_grid_inventory$setGridInventoryData(data.copy());
        }
    }

    @Override
    public GridInventoryData copyPlayerGridInventory(Player player) {
        return getPlayerGridInventory(player).copy();
    }

    public static GridInventoryData createDefault() {
        int columns = 4;
        int rows = 1;
        try {
            columns = GridInventoryServices.config().pocketColumns();
            rows = GridInventoryServices.config().pocketRows();
        } catch (IllegalStateException ignored) {
            // Services can be unavailable while mixins construct very early.
        }
        return new GridInventoryData(columns, rows);
    }
}
