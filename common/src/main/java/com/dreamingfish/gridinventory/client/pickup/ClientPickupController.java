package com.dreamingfish.gridinventory.client.pickup;

import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.common.network.ManualPickupItemPacket;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public final class ClientPickupController {
    private static int attractEntityId = -1;
    private static int attractTicks;

    private ClientPickupController() {
    }

    public static void tick() {
        while (ModKeyMappings.PICKUP_ITEM.consumeClick()) {
            int target = ClientItemTargeting.currentTargetItemEntityId();
            if (target >= 0) {
                requestPickup(target);
            }
        }
        tickAttractEffect();
    }

    public static void requestPickup(int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.swing(InteractionHand.MAIN_HAND);
        }
        attractEntityId = entityId;
        attractTicks = 8;
        GridInventoryServices.network().sendToServer(new ManualPickupItemPacket(entityId));
    }

    private static void tickAttractEffect() {
        if (attractEntityId < 0 || attractTicks <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            attractEntityId = -1;
            attractTicks = 0;
            return;
        }
        Entity entity = minecraft.level.getEntity(attractEntityId);
        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.isRemoved()) {
            attractEntityId = -1;
            attractTicks = 0;
            return;
        }
        double progress = 1.0 - attractTicks / 8.0;
        double pull = 0.25 + progress * 0.35;
        var target = minecraft.player.position().add(0.0, minecraft.player.getBbHeight() * 0.45, 0.0);
        var next = itemEntity.position().lerp(target, pull);
        itemEntity.setPos(next.x, next.y, next.z);
        attractTicks--;
    }
}
