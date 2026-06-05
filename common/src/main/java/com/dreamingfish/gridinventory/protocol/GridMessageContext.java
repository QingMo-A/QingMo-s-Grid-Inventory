package com.dreamingfish.gridinventory.protocol;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface GridMessageContext {
    Player player();

    default ServerPlayer serverPlayerOrNull() {
        return player() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }

    void enqueueWork(Runnable task);

    void markHandled();
}
