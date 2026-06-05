package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class NeoForge1211MessageContext implements GridMessageContext {
    private final IPayloadContext context;

    public NeoForge1211MessageContext(IPayloadContext context) {
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

    @Override
    public void markHandled() {
    }
}
