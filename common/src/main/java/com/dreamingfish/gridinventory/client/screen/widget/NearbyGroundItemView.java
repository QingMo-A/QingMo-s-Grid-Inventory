package com.dreamingfish.gridinventory.client.screen.widget;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public record NearbyGroundItemView(int entityId, ItemStack stack, double distance, int gridWidth, int gridHeight, boolean rotatable, Vec3 worldPosition) {
}
