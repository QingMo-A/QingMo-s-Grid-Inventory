package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;

public interface NeoForge1211MessageCodec<T extends GridMessage> {
    void encode(T message, RegistryFriendlyByteBuf buf);

    T decode(RegistryFriendlyByteBuf buf);
}
