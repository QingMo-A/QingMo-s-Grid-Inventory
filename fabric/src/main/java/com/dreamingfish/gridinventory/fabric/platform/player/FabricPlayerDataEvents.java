package com.dreamingfish.gridinventory.fabric.platform.player;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public final class FabricPlayerDataEvents {
    private FabricPlayerDataEvents() {
    }

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            GridInventoryData oldData = GridInventoryServices.playerData().copyPlayerGridInventory(oldPlayer);
            GridInventoryServices.playerData().setPlayerGridInventory(newPlayer, oldData);
        });
    }
}
