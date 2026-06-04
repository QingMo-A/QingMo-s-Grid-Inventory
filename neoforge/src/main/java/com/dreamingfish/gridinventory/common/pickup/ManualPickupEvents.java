package com.dreamingfish.gridinventory.common.pickup;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class ManualPickupEvents {
    private ManualPickupEvents() {
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (GridInventoryServices.config().disableVanillaAutoPickup() && event.getPlayer() instanceof ServerPlayer) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}
