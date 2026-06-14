package com.dreamingfish.gridinventory.common.pickup;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionManager;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.ModNetworking;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class ManualPickupHandler {
    private ManualPickupHandler() {
    }

    public static boolean tryPickupToPlayerInventory(ServerPlayer player, int entityId) {
        if (!GridInventoryServices.config().manualPickupEnabled()) {
            return false;
        }
        Optional<ItemEntity> reachable = findReachableItem(player, entityId);
        if (reachable.isEmpty()) {
            return false;
        }
        ItemEntity itemEntity = reachable.get();
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return false;
        }

        int originalCount = groundStack.getCount();
        ItemStack toInsert = groundStack.copy();
        toInsert = insertIntoPlayerSlots(player, toInsert, 0, 9);
        if (GridInventoryServices.config().replaceSurvivalInventory()) {
            toInsert = insertIntoPocket(player, toInsert);
        } else {
            toInsert = insertIntoPlayerSlots(player, toInsert, 9, 36);
        }
        int inserted = originalCount - toInsert.getCount();
        if (inserted <= 0) {
            return false;
        }

        if (inserted >= originalCount) {
            player.take(itemEntity, inserted);
            itemEntity.discard();
        } else {
            player.take(itemEntity, inserted);
            groundStack.shrink(inserted);
        }
        player.inventoryMenu.broadcastChanges();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        return true;
    }

    private static ItemStack insertIntoPlayerSlots(ServerPlayer player, ItemStack stack, int startInclusive, int endExclusive) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        for (int slot = startInclusive; slot < endExclusive && !remainder.isEmpty(); slot++) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (existing.isEmpty() || !GridItemStacks.sameItemSameData(existing, remainder)) {
                continue;
            }
            int limit = Math.min(existing.getMaxStackSize(), player.getInventory().getMaxStackSize());
            int moved = Math.min(remainder.getCount(), limit - existing.getCount());
            if (moved > 0) {
                existing.grow(moved);
                remainder.shrink(moved);
                player.getInventory().setChanged();
            }
        }
        for (int slot = startInclusive; slot < endExclusive && !remainder.isEmpty(); slot++) {
            ItemStack existing = player.getInventory().getItem(slot);
            if (!existing.isEmpty()) {
                continue;
            }
            int moved = Math.min(remainder.getCount(), Math.min(remainder.getMaxStackSize(), player.getInventory().getMaxStackSize()));
            player.getInventory().setItem(slot, remainder.copyWithCount(moved));
            remainder.shrink(moved);
            player.getInventory().setChanged();
        }
        return remainder;
    }

    public static Optional<ItemEntity> findReachableItem(ServerPlayer player, int entityId) {
        if (!GridInventoryServices.config().manualPickupEnabled()) {
            return Optional.empty();
        }
        Entity entity = player.serverLevel().getEntity(entityId);
        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.isRemoved() || !itemEntity.isAlive()) {
            return Optional.empty();
        }
        if (itemEntity.level() != player.level()) {
            return Optional.empty();
        }
        double range = GridInventoryServices.config().pickupRange();
        if (distanceToItemBoundsSqr(player, itemEntity) > range * range) {
            return Optional.empty();
        }
        if (!GridInventoryServices.config().allowPickupThroughWalls() && !hasLineOfSight(player, itemEntity)) {
            return Optional.empty();
        }
        return Optional.of(itemEntity);
    }

    private static ItemStack insertIntoPocket(ServerPlayer player, ItemStack stack) {
        if (player.containerMenu instanceof GridInventoryMenu menu && menu.isPlayerGrid()) {
            ItemStack remainder = menu.getGridData().insert(stack, GridInsertMode.EXECUTE);
            menu.save();
            ModNetworking.syncMenu(player, menu);
            return remainder;
        }
        GridInventoryData pocket = PlayerPocketDefinitionManager.refreshShape(GridInventoryServices.playerData().getPlayerGridInventory(player)).copy();
        ItemStack remainder = pocket.insert(stack, GridInsertMode.EXECUTE);
        if (remainder.getCount() != stack.getCount()) {
            GridInventoryServices.playerData().setPlayerGridInventory(player, pocket);
        }
        return remainder;
    }

    private static boolean hasLineOfSight(ServerPlayer player, ItemEntity itemEntity) {
        Vec3 eye = player.getEyePosition();
        AABB bounds = itemEntity.getBoundingBox().inflate(0.12);
        Vec3 center = bounds.getCenter();
        Vec3 top = new Vec3(center.x, bounds.maxY + 0.18, center.z);
        Vec3 playerSide = center.add(eye.subtract(center).normalize().scale(Math.min(0.25, bounds.getXsize() + 0.12)));
        return canSeePoint(player, eye, bounds, center)
                || canSeePoint(player, eye, bounds, top)
                || canSeePoint(player, eye, bounds, playerSide);
    }

    private static boolean canSeePoint(ServerPlayer player, Vec3 eye, AABB targetBounds, Vec3 target) {
        HitResult hit = player.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || targetBounds.contains(hit.getLocation());
    }

    private static double distanceToItemBoundsSqr(ServerPlayer player, ItemEntity itemEntity) {
        Vec3 eye = player.getEyePosition();
        AABB bounds = itemEntity.getBoundingBox().inflate(0.3);
        double x = clamp(eye.x, bounds.minX, bounds.maxX);
        double y = clamp(eye.y, bounds.minY, bounds.maxY);
        double z = clamp(eye.z, bounds.minZ, bounds.maxZ);
        return eye.distanceToSqr(x, y, z);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
