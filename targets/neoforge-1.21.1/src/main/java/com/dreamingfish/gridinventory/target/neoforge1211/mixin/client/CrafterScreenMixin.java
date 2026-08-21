package com.dreamingfish.gridinventory.target.neoforge1211.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CrafterMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CrafterScreen.class)
public abstract class CrafterScreenMixin extends AbstractContainerScreen<CrafterMenu> {
    protected CrafterScreenMixin(CrafterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @ModifyConstant(method = "renderRedstone", constant = @Constant(intValue = 48))
    private int df_grid_inventory$keepRedstoneIconWithCompactPanel(int originalOffset) {
        return imageHeight / 2 - 35;
    }
}
