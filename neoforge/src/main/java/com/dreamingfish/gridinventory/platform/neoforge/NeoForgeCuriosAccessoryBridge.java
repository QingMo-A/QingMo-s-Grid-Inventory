package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;

public final class NeoForgeCuriosAccessoryBridge implements GridInventoryAccessoryBridge {
    @Override
    public boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    @Override
    public boolean canQuickEquip(Player player, ItemStack stack) {
        return CuriosIntegration.canQuickEquip(player, stack);
    }

    @Override
    public boolean quickEquip(Player player, ItemStack source) {
        return CuriosIntegration.quickEquip(player, source);
    }

    @Override
    public List<CuriosSlotView> collectSlots(Player player) {
        return CuriosIntegration.collectSlots(player);
    }
}
