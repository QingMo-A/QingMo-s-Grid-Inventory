package com.dreamingfish.gridinventory.target.forge1201.protocol;

public final class Forge1201ProtocolCompat {
    private Forge1201ProtocolCompat() {
    }

    public static void register() {
        Forge1201SimpleChannelBridge.register();
    }
}
