package com.dreamingfish.gridinventory.target.neoforge1211.mixin.client;

import com.dreamingfish.gridinventory.client.screen.panel.VanillaContainerScreenLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin<T extends AbstractFurnaceMenu> extends AbstractContainerScreen<T> {
    @Unique
    private static final ResourceLocation DF_GRID_INVENTORY$GENERIC_CONTAINER_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    protected AbstractFurnaceScreenMixin(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 49))
    private int df_grid_inventory$keepRecipeButtonWithCompactPanel(int originalOffset) {
        return imageHeight / 2 - 34;
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void df_grid_inventory$renderCompactBottomCap(GuiGraphics graphics, float partialTick,
                                                           int mouseX, int mouseY, CallbackInfo ci) {
        if (imageHeight < 166) {
            VanillaContainerScreenLayout.renderBottomCap(graphics, DF_GRID_INVENTORY$GENERIC_CONTAINER_TEXTURE,
                    leftPos, topPos + imageHeight - VanillaContainerScreenLayout.BOTTOM_CAP_HEIGHT, imageWidth);
        }
    }
}
