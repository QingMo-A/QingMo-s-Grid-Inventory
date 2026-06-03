package com.dreamingfish.gridinventory.common.folding;

import com.dreamingfish.gridinventory.common.network.SyncBackpackFoldingRulesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BackpackFoldingSyncManager {
    private BackpackFoldingSyncManager() {
    }

    public static void syncTo(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncBackpackFoldingRulesPacket(BackpackFoldingManager.getRules()));
    }

    public static void syncToAll() {
        PacketDistributor.sendToAllPlayers(new SyncBackpackFoldingRulesPacket(BackpackFoldingManager.getRules()));
    }
}
