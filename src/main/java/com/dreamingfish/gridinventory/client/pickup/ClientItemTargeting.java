package com.dreamingfish.gridinventory.client.pickup;

import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public final class ClientItemTargeting {
    private static int currentTargetItemEntityId = -1;

    private ClientItemTargeting() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null || !GridInventoryClientConfig.HIGHLIGHT_TARGET_ITEM.get()) {
            currentTargetItemEntityId = -1;
            return;
        }
        double range = GridInventoryConfig.PICKUP_RANGE.get();
        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 look = minecraft.player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        AABB searchBox = minecraft.player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);

        Optional<ItemEntity> target = minecraft.level.getEntitiesOfClass(ItemEntity.class, searchBox, item -> !item.isRemoved() && item.isAlive() && !item.getItem().isEmpty())
                .stream()
                .filter(item -> item.getBoundingBox().inflate(0.3).clip(eye, end).isPresent())
                .min(Comparator.comparingDouble(item -> item.distanceToSqr(minecraft.player)));

        currentTargetItemEntityId = target.map(ItemEntity::getId).orElse(-1);
    }

    public static int currentTargetItemEntityId() {
        return currentTargetItemEntityId;
    }
}
