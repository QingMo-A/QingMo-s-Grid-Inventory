package com.dreamingfish.gridinventory.platform;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface GridInventoryAccessoryBridge {
    boolean isLoaded();

    boolean canQuickEquip(Player player, ItemStack stack);

    boolean quickEquip(Player player, ItemStack source);

    List<CuriosSlotView> collectSlots(Player player);
}
