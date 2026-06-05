package com.dreamingfish.gridinventory.platform.network;

import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import net.minecraft.server.level.ServerPlayer;

@Deprecated(forRemoval = true)
public interface GridPacketContext extends GridMessageContext {
    default ServerPlayer serverPlayer() {
        return serverPlayerOrNull();
    }
}
