package com.dreamingfish.gridinventory.target.forge1201.protocol;

public final class Forge1201ProtocolCompat {
    private Forge1201ProtocolCompat() {
    }

    public static void register() {
        Forge1201SimpleChannelBridge.register();
    }

    public static void sendToServer(com.dreamingfish.gridinventory.protocol.GridMessage message) {
        Forge1201SimpleChannelBridge.sendToServer(message);
    }

    public static void sendToPlayer(net.minecraft.server.level.ServerPlayer player, com.dreamingfish.gridinventory.protocol.GridMessage message) {
        Forge1201SimpleChannelBridge.sendToPlayer(player, message);
    }

    public static void sendToAllPlayers(com.dreamingfish.gridinventory.protocol.GridMessage message) {
        Forge1201SimpleChannelBridge.sendToAllPlayers(message);
    }
}
