package com.dreamingfish.gridinventory.fabric;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.DFGridInventoryCommon;
import com.dreamingfish.gridinventory.fabric.platform.FabricGridInventoryPlatform;
import com.dreamingfish.gridinventory.fabric.platform.network.FabricNetworking;
import com.dreamingfish.gridinventory.fabric.platform.player.FabricPlayerDataEvents;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.fabricmc.api.ModInitializer;

public final class DFGridInventoryFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricGridInventoryPlatform platform = new FabricGridInventoryPlatform();
        GridInventoryServices.init(platform);
        DFGridInventoryCommon.registerContent(GridInventoryServices.registry());
        FabricNetworking.registerCommonPackets();
        FabricNetworking.registerServerReceivers();
        FabricPlayerDataEvents.register();
        DFGridInventory.LOGGER.info("DF Grid Inventory Fabric network bootstrap loaded.");
    }
}
