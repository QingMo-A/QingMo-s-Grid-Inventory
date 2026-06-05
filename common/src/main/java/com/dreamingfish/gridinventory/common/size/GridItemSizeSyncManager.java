package com.dreamingfish.gridinventory.common.size;

import com.dreamingfish.gridinventory.common.network.SyncItemSizeRulesMessage;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;

public final class GridItemSizeSyncManager {
    private GridItemSizeSyncManager() {
    }

    public static void syncTo(ServerPlayer player) {
        GridInventoryServices.network().sendToPlayer(player, new SyncItemSizeRulesMessage(GridItemSizeManager.getRules()));
    }

    public static void syncToAll() {
        GridInventoryServices.network().sendToAllPlayers(new SyncItemSizeRulesMessage(GridItemSizeManager.getRules()));
    }
}
