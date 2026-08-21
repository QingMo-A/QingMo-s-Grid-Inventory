package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import com.dreamingfish.gridinventory.client.screen.panel.VanillaContainerScreenLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends AbstractContainerScreen<CraftingMenu> {
    @Shadow private boolean widthTooNarrow;

    @Unique
    private static final ResourceLocation DF_GRID_INVENTORY$CRAFTING_TABLE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");

    protected CraftingScreenMixin(CraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;init(IILnet/minecraft/client/Minecraft;ZLnet/minecraft/world/inventory/RecipeBookMenu;)V"), index = 1)
    private int df_grid_inventory$alignRecipeBookWithCompactPanel(int screenHeight) {
        return VanillaContainerScreenLayout.alignedRecipeBookScreenHeight(screenHeight, imageHeight);
    }

    @ModifyConstant(method = "init", constant = @Constant(intValue = 49))
    private int df_grid_inventory$keepRecipeButtonWithCompactPanel(int originalOffset) {
        return imageHeight / 2 - 34;
    }

    @ModifyConstant(method = "lambda$init$0", constant = @Constant(intValue = 49), remap = false)
    private int df_grid_inventory$keepToggledRecipeButtonWithCompactPanel(int originalOffset) {
        return imageHeight / 2 - 34;
    }

    @Redirect(method = "hasClickedOutside", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;hasClickedOutside(DDIIIII)Z"))
    private boolean df_grid_inventory$keepFullRecipeBookInteractionArea(RecipeBookComponent recipeBook,
                                                                        double mouseX, double mouseY,
                                                                        int menuLeft, int menuTop,
                                                                        int menuWidth, int menuHeight, int button) {
        if (recipeBook.isVisible()
                && VanillaContainerScreenLayout.isInsideAlignedRecipeBook(mouseX, mouseY,
                width, height, imageHeight, widthTooNarrow)) {
            return false;
        }
        return recipeBook.hasClickedOutside(mouseX, mouseY, menuLeft, menuTop,
                menuWidth, menuHeight, button);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void df_grid_inventory$renderCompactBottomCap(GuiGraphics graphics, float partialTick,
                                                           int mouseX, int mouseY, CallbackInfo ci) {
        if (imageHeight < 166) {
            VanillaContainerScreenLayout.renderBottomCap(graphics, DF_GRID_INVENTORY$CRAFTING_TABLE_TEXTURE,
                    VanillaContainerScreenLayout.STANDARD_BOTTOM_CAP_SOURCE_Y,
                    VanillaContainerScreenLayout.STANDARD_IMAGE_WIDTH,
                    leftPos, topPos + imageHeight - VanillaContainerScreenLayout.BOTTOM_CAP_HEIGHT, imageWidth);
        }
    }
}
