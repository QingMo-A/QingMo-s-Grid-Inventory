package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import com.dreamingfish.gridinventory.client.access.SlotPositionAccessor;
import com.dreamingfish.gridinventory.client.screen.panel.ExternalContainerSidebar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {
    @Unique
    private static final int DF_GRID_INVENTORY$HIDDEN_SLOT_COORDINATE = -10_000;
    @Unique
    private static final int DF_GRID_INVENTORY$BOTTOM_CAP_SOURCE_Y = 215;
    @Unique
    private static final int DF_GRID_INVENTORY$BOTTOM_CAP_HEIGHT = 7;

    @Shadow @Final private int containerRows;

    @Unique
    private boolean df_grid_inventory$compact;

    protected ContainerScreenMixin(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void df_grid_inventory$compactVanillaChest(ChestMenu menu, Inventory playerInventory,
                                                       Component title, CallbackInfo ci) {
        df_grid_inventory$compact = ExternalContainerSidebar.supports(this, menu);
        if (!df_grid_inventory$compact) {
            return;
        }

        imageHeight = containerRows * 18 + 17 + DF_GRID_INVENTORY$BOTTOM_CAP_HEIGHT;
        inventoryLabelY = DF_GRID_INVENTORY$HIDDEN_SLOT_COORDINATE;
        for (Slot slot : menu.slots) {
            if (slot.container == playerInventory) {
                SlotPositionAccessor position = (SlotPositionAccessor) slot;
                position.df_grid_inventory$setX(DF_GRID_INVENTORY$HIDDEN_SLOT_COORDINATE);
                position.df_grid_inventory$setY(DF_GRID_INVENTORY$HIDDEN_SLOT_COORDINATE);
            }
        }
    }

    @Redirect(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 1
            )
    )
    private void df_grid_inventory$skipPlayerInventoryBackground(GuiGraphics graphics, ResourceLocation texture,
                                                                  int x, int y, int u, int v,
                                                                  int width, int height) {
        if (df_grid_inventory$compact) {
            graphics.blit(texture, x, y, u, DF_GRID_INVENTORY$BOTTOM_CAP_SOURCE_Y,
                    width, DF_GRID_INVENTORY$BOTTOM_CAP_HEIGHT);
            return;
        }
        graphics.blit(texture, x, y, u, v, width, height);
    }
}
