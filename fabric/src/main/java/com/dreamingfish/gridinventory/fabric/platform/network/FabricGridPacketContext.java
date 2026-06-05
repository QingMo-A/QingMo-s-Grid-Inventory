package com.dreamingfish.gridinventory.fabric.platform.network;

import com.dreamingfish.gridinventory.platform.network.GridPacketContext;
import net.minecraft.world.entity.player.Player;

public final class FabricGridPacketContext implements GridPacketContext {
    private final Player player;

    public FabricGridPacketContext(Player player) {
        this.player = player;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public void enqueueWork(Runnable task) {
        task.run();
    }
}
