package com.dreamingfish.gridinventory.fabric.client;

import com.dreamingfish.gridinventory.fabric.platform.network.FabricClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class DFGridInventoryFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientScreens.register();
        FabricClientKeyMappings.register();
        FabricClientEvents.register();
        FabricItemProperties.register();
        FabricClientNetworking.registerClientReceivers();
    }
}
