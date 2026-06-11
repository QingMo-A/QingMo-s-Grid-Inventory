package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class Forge1201MessageContext implements GridMessageContext {
    private final Supplier<NetworkEvent.Context> context;

    public Forge1201MessageContext(Supplier<NetworkEvent.Context> context) {
        this.context = context;
    }

    @Override
    public Player player() {
        Player sender = context.get().getSender();
        if (sender != null) {
            return sender;
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientOnly::player);
    }

    @Override
    public void enqueueWork(Runnable task) {
        context.get().enqueueWork(task);
    }

    @Override
    public void markHandled() {
        context.get().setPacketHandled(true);
    }

    private static final class ClientOnly {
        private static Player player() {
            return net.minecraft.client.Minecraft.getInstance().player;
        }
    }
}
