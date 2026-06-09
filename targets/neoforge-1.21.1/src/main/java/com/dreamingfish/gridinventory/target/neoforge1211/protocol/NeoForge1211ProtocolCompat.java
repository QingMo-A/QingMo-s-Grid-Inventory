package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.common.network.GridMessages;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageDirection;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForge1211ProtocolCompat {
    private NeoForge1211ProtocolCompat() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (GridMessageType<?> messageType : GridMessages.REGISTRY.messages()) {
            register(registrar, messageType);
        }
    }

    public static void sendToServer(GridMessage message) {
        PacketDistributor.sendToServer(NeoForge1211PayloadAdapters.payload(cast(message.type()), message));
    }

    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, GridMessage message) {
        PacketDistributor.sendToPlayer(player, NeoForge1211PayloadAdapters.payload(cast(message.type()), message));
    }

    public static void sendToAllPlayers(GridMessage message) {
        PacketDistributor.sendToAllPlayers(NeoForge1211PayloadAdapters.payload(cast(message.type()), message));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends GridMessage> void register(PayloadRegistrar registrar, GridMessageType<T> messageType) {
        var payloadType = NeoForge1211PayloadAdapters.payloadType(messageType);
        var codec = NeoForge1211PayloadAdapters.codec(messageType);
        if (messageType.direction() == GridMessageDirection.SERVER_TO_CLIENT) {
            registrar.playToClient(payloadType, codec, NeoForge1211ProtocolCompat::handlePayload);
        } else if (messageType.direction() == GridMessageDirection.CLIENT_TO_SERVER) {
            registrar.playToServer(payloadType, codec, NeoForge1211ProtocolCompat::handlePayload);
        } else {
            registrar.playBidirectional(payloadType, codec, NeoForge1211ProtocolCompat::handlePayload);
        }
    }

    private static <T extends GridMessage> void handlePayload(NeoForge1211PayloadAdapters.Payload<T> payload, IPayloadContext context) {
        NeoForge1211MessageContext messageContext = new NeoForge1211MessageContext(context);
        messageContext.enqueueWork(() -> payload.messageType().handle(payload.message(), messageContext));
    }

    @SuppressWarnings("unchecked")
    private static <T extends GridMessage> GridMessageType<T> cast(GridMessageType<?> type) {
        return (GridMessageType<T>) type;
    }
}
