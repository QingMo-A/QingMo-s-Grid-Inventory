package com.dreamingfish.gridinventory.common.size;

import com.dreamingfish.gridinventory.common.network.SyncItemSizeRulesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GridItemSizeSyncManager {
    private GridItemSizeSyncManager() {
    }

    public static void syncTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncItemSizeRulesPacket(GridItemSizeManager.getRules()));
    }

    public static void syncToAll() {
        PacketDistributor.sendToAllPlayers(new SyncItemSizeRulesPacket(GridItemSizeManager.getRules()));
    }
}
