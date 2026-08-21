package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin {
    @ModifyArg(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 6
            ),
            index = 4
    )
    private int df_grid_inventory$restorePatternButtonSourceY(int sourceY) {
        return sourceY < 166 ? sourceY + 76 : sourceY;
    }
}
