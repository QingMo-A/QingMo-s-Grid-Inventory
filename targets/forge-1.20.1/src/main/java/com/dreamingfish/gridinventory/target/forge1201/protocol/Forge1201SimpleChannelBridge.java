package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageDirection;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class Forge1201SimpleChannelBridge {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DFGridInventory.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int discriminator;
    private static boolean registered;

    private Forge1201SimpleChannelBridge() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        for (GridMessageType<?> type : Forge1201MessageCodecs.registeredTypes()) {
            register(type);
        }
    }

    public static void sendToServer(GridMessage message) {
        Forge1201MessageCodecs.codec(cast(message.type()));
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(ServerPlayer player, GridMessage message) {
        Forge1201MessageCodecs.codec(cast(message.type()));
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToAllPlayers(GridMessage message) {
        Forge1201MessageCodecs.codec(cast(message.type()));
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    private static <T extends GridMessage> void register(GridMessageType<T> type) {
        CHANNEL.messageBuilder(type.messageClass(), discriminator++, direction(type))
                .encoder(Forge1201MessageCodecs::encode)
                .decoder(buf -> Forge1201MessageCodecs.decode(type, buf))
                .consumerMainThread((message, context) -> handle(type, message, context))
                .add();
    }

    private static NetworkDirection direction(GridMessageType<?> type) {
        if (type.direction() == GridMessageDirection.SERVER_TO_CLIENT) {
            return NetworkDirection.PLAY_TO_CLIENT;
        }
        if (type.direction() == GridMessageDirection.CLIENT_TO_SERVER) {
            return NetworkDirection.PLAY_TO_SERVER;
        }
        throw new IllegalStateException("Forge 1.20.1 SimpleChannel does not register bidirectional messages in this skeleton: " + type.id());
    }

    private static <T extends GridMessage> void handle(GridMessageType<T> type, T message, Supplier<NetworkEvent.Context> context) {
        Forge1201MessageContext messageContext = new Forge1201MessageContext(context);
        messageContext.enqueueWork(() -> type.handle(message, messageContext));
        messageContext.markHandled();
    }

    @SuppressWarnings("unchecked")
    private static <T extends GridMessage> GridMessageType<T> cast(GridMessageType<?> type) {
        return (GridMessageType<T>) type;
    }
}
