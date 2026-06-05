package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class NeoForge1211MessageCodecs {
    private NeoForge1211MessageCodecs() {
    }

    public static void encode(GridMessage message, FriendlyByteBuf buf) {
        try {
            Method encode = findBufferMethod(message.getClass(), "encode");
            encode.invoke(message, buf);
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to encode grid message " + message.type().id(), exception);
        }
    }

    public static <T extends GridMessage> T decode(GridMessageType<T> type, FriendlyByteBuf buf) {
        try {
            Method decode = findBufferMethod(type.messageClass(), "decode");
            return type.messageClass().cast(decode.invoke(null, buf));
        } catch (NoSuchMethodException ignored) {
            return constructUnit(type);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to decode grid message " + type.id(), exception);
        }
    }

    private static Method findBufferMethod(Class<?> messageClass, String name) throws NoSuchMethodException {
        try {
            return messageClass.getMethod(name, FriendlyByteBuf.class);
        } catch (NoSuchMethodException ignored) {
            return messageClass.getMethod(name, RegistryFriendlyByteBuf.class);
        }
    }

    private static <T extends GridMessage> T constructUnit(GridMessageType<T> type) {
        try {
            Constructor<T> constructor = type.messageClass().getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Grid message has no decode(FriendlyByteBuf) or unit constructor: " + type.id(), exception);
        }
    }
}
