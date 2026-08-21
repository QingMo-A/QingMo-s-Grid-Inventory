package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {
    @ModifyArg(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            ),
            index = 4
    )
    private int df_grid_inventory$restoreTextFieldSourceY(int sourceY) {
        return sourceY < 166 ? sourceY + 76 : sourceY;
    }
}
