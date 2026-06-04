package com.dreamingfish.gridinventory.platform.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface GridPacketContext {
    Player player();

    default ServerPlayer serverPlayer() {
        return player() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    void enqueueWork(Runnable task);
}
