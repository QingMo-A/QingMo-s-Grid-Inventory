package com.dreamingfish.gridinventory.target.neoforge1211.event;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class NeoForge1211ManualPickupEvents {
    private NeoForge1211ManualPickupEvents() {
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (GridInventoryServices.config().disableVanillaAutoPickup() && event.getPlayer() instanceof ServerPlayer) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}
