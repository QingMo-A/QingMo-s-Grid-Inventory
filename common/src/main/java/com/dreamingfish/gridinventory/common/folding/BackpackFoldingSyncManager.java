package com.dreamingfish.gridinventory.common.folding;

import com.dreamingfish.gridinventory.common.network.SyncBackpackFoldingRulesPacket;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;

public final class BackpackFoldingSyncManager {
    private BackpackFoldingSyncManager() {
    }

    public static void syncTo(ServerPlayer player) {
        GridInventoryServices.network().sendToPlayer(player, new SyncBackpackFoldingRulesPacket(BackpackFoldingManager.getRules()));
    }

    public static void syncToAll() {
        GridInventoryServices.network().sendToAllPlayers(new SyncBackpackFoldingRulesPacket(BackpackFoldingManager.getRules()));
    }
}
