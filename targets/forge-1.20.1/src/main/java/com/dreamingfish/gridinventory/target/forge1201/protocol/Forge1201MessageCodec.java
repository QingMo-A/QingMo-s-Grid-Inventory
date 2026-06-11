package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import net.minecraft.network.FriendlyByteBuf;

public interface Forge1201MessageCodec<T extends GridMessage> {
    void encode(T message, FriendlyByteBuf buf);

    T decode(FriendlyByteBuf buf);
}
