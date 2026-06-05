package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class Forge1201MessageCodecs {
    private Forge1201MessageCodecs() {
    }

    public static void encode(GridMessage message, FriendlyByteBuf buf) {
        try {
            Method encode = message.getClass().getMethod("encode", FriendlyByteBuf.class);
            encode.invoke(message, buf);
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to encode grid message " + message.type().id(), exception);
        }
    }

    public static <T extends GridMessage> T decode(GridMessageType<T> type, FriendlyByteBuf buf) {
        try {
            Method decode = type.messageClass().getMethod("decode", FriendlyByteBuf.class);
            return type.messageClass().cast(decode.invoke(null, buf));
        } catch (NoSuchMethodException ignored) {
            return constructUnit(type);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to decode grid message " + type.id(), exception);
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
