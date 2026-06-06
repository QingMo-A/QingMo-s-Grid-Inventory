package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.world.InteractionHand;

public record GridInventoryMenuOpenData(int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data) {
}
