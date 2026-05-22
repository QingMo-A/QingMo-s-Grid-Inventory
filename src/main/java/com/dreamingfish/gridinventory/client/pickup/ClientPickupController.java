package com.dreamingfish.gridinventory.client.pickup;

import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.common.network.ManualPickupItemPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientPickupController {
    private ClientPickupController() {
    }

    public static void tick() {
        while (ModKeyMappings.PICKUP_ITEM.consumeClick()) {
            int target = ClientItemTargeting.currentTargetItemEntityId();
            if (target >= 0) {
                PacketDistributor.sendToServer(new ManualPickupItemPacket(target));
            }
        }
    }
}
