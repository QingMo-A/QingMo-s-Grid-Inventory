package com.dreamingfish.gridinventory.common.equipment;

import net.minecraft.world.entity.EquipmentSlot;

public final class GridEquipmentSlots {
    private GridEquipmentSlots() {
    }

    public static EquipmentSlot back() {
        try {
            return EquipmentSlot.byName("body");
        } catch (IllegalArgumentException ignored) {
            return EquipmentSlot.OFFHAND;
        }
    }

    public static boolean isBack(EquipmentSlot slot) {
        return slot == back();
    }
}
