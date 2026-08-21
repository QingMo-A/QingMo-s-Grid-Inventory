package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenMixin {
    @ModifyArg(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            ),
            index = 4
    )
    private int df_grid_inventory$restoreHorseOverlaySourceY(int sourceY) {
        return sourceY > 0 && sourceY < 166 ? sourceY + 76 : sourceY;
    }
}
