package com.dreamingfish.gridinventory.common.pickup;

import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.api.GridInsertMode;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.network.ModNetworking;
import com.dreamingfish.gridinventory.common.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ManualPickupHandler {
    private ManualPickupHandler() {
    }

    public static boolean tryPickupToPlayerInventory(ServerPlayer player, int entityId) {
        if (!GridInventoryConfig.MANUAL_PICKUP_ENABLED.get()) {
            return false;
        }
        Entity entity = player.serverLevel().getEntity(entityId);
        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.isRemoved() || !itemEntity.isAlive()) {
            return false;
        }
        if (itemEntity.level() != player.level()) {
            return false;
        }
        double range = GridInventoryConfig.PICKUP_RANGE.get();
        if (player.distanceToSqr(itemEntity) > range * range) {
            return false;
        }
        if (!GridInventoryConfig.ALLOW_PICKUP_THROUGH_WALLS.get() && !hasLineOfSight(player, itemEntity)) {
            return false;
        }
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return false;
        }

        int originalCount = groundStack.getCount();
        ItemStack toInsert = groundStack.copy();
        if (GridInventoryConfig.REPLACE_SURVIVAL_INVENTORY.get()) {
            toInsert = insertIntoPocket(player, toInsert);
        } else {
            player.getInventory().add(toInsert);
        }
        int inserted = originalCount - toInsert.getCount();
        if (inserted <= 0) {
            return false;
        }

        if (inserted >= originalCount) {
            itemEntity.discard();
        } else {
            groundStack.shrink(inserted);
        }
        player.inventoryMenu.broadcastChanges();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        return true;
    }

    private static ItemStack insertIntoPocket(ServerPlayer player, ItemStack stack) {
        if (player.containerMenu instanceof GridInventoryMenu menu && menu.isPlayerGrid()) {
            ItemStack remainder = menu.getGridData().insert(stack, GridInsertMode.EXECUTE);
            menu.save();
            ModNetworking.syncMenu(player, menu);
            return remainder;
        }
        GridInventoryData pocket = player.getData(ModAttachments.PLAYER_GRID_INVENTORY).copy();
        ItemStack remainder = pocket.insert(stack, GridInsertMode.EXECUTE);
        if (remainder.getCount() != stack.getCount()) {
            player.setData(ModAttachments.PLAYER_GRID_INVENTORY, pocket);
        }
        return remainder;
    }

    private static boolean hasLineOfSight(ServerPlayer player, ItemEntity itemEntity) {
        Vec3 eye = player.getEyePosition();
        Vec3 target = itemEntity.position().add(0.0, itemEntity.getBbHeight() * 0.5, 0.0);
        HitResult hit = player.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(target) < 0.25;
    }
}
