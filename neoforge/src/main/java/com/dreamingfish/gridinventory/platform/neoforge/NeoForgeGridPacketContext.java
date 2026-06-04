package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.platform.network.GridPacketContext;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NeoForgeGridPacketContext implements GridPacketContext {
    private final IPayloadContext context;

    public NeoForgeGridPacketContext(IPayloadContext context) {
        this.context = context;
    }

    @Override
    public Player player() {
        return context.player();
    }

    @Override
    public void enqueueWork(Runnable task) {
        context.enqueueWork(task);
    }
}
