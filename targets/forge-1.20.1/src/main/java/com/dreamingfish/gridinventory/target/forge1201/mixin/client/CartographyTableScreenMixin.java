package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin {
    @ModifyArg(
            method = "renderResultingMap",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 4
            ),
            index = 4
    )
    private int df_grid_inventory$restoreLockedMapSourceY(int sourceY) {
        return sourceY < 166 ? sourceY + 76 : sourceY;
    }
}
