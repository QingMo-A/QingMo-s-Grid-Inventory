package com.dreamingfish.gridinventory.common.equipment;

import com.dreamingfish.gridinventory.target.neoforge1211.registry.NeoForge1211DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class EquipmentStorageEvents {
    private EquipmentStorageEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        initializeEquippedStorage(player, EquipmentSlot.CHEST);
        initializeEquippedStorage(player, EquipmentSlot.LEGS);
    }

    private static void initializeEquippedStorage(ServerPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty() || stack.get(NeoForge1211DataComponents.EQUIPMENT_STORAGE.get()) != null) {
            return;
        }
        if (!EquipmentStorageManager.initializeStorage(stack, slot).containers().isEmpty()) {
            player.getInventory().setChanged();
        }
    }
}
