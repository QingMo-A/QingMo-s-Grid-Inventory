package com.dreamingfish.gridinventory.common.pickup;

import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

public final class ManualPickupEvents {
    private ManualPickupEvents() {
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (GridInventoryConfig.DISABLE_VANILLA_AUTO_PICKUP.get() && event.getPlayer() instanceof ServerPlayer) {
            event.setCanPickup(TriState.FALSE);
        }
    }
}
