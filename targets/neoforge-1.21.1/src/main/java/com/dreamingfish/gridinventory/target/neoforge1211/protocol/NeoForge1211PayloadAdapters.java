package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class NeoForge1211PayloadAdapters {
    private NeoForge1211PayloadAdapters() {
    }

    public record Payload<T extends GridMessage>(
            Type<Payload<T>> payloadType,
            GridMessageType<T> messageType,
            T message
    ) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return payloadType;
        }

        public void encode(RegistryFriendlyByteBuf buf) {
            NeoForge1211MessageCodecs.codec(messageType).encode(message, buf);
        }
    }

    public static <T extends GridMessage> CustomPacketPayload.Type<Payload<T>> payloadType(GridMessageType<T> messageType) {
        return new CustomPacketPayload.Type<>(messageType.id());
    }

    public static <T extends GridMessage> Payload<T> payload(GridMessageType<T> messageType, T message) {
        return new Payload<>(payloadType(messageType), messageType, message);
    }

    public static <T extends GridMessage> StreamCodec<RegistryFriendlyByteBuf, Payload<T>> codec(GridMessageType<T> messageType) {
        CustomPacketPayload.Type<Payload<T>> payloadType = payloadType(messageType);
        NeoForge1211MessageCodec<T> messageCodec = NeoForge1211MessageCodecs.codec(messageType);
        return StreamCodec.ofMember(Payload::encode, buf -> new Payload<>(payloadType, messageType, messageCodec.decode(buf)));
    }
}
