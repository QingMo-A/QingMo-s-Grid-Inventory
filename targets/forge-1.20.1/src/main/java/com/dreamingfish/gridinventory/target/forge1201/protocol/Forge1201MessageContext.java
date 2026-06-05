package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class Forge1201MessageContext implements GridMessageContext {
    private final Supplier<NetworkEvent.Context> context;

    public Forge1201MessageContext(Supplier<NetworkEvent.Context> context) {
        this.context = context;
    }

    @Override
    public Player player() {
        return context.get().getSender();
    }

    @Override
    public void enqueueWork(Runnable task) {
        context.get().enqueueWork(task);
    }

    @Override
    public void markHandled() {
        context.get().setPacketHandled(true);
    }
}
