package com.dreamingfish.gridinventory.mixin.client;

import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    @Unique
    private final NearbyItemsPanel df_grid_inventory$nearbyItemsPanel = new NearbyItemsPanel();

    private InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void df_grid_inventory$renderNearbyItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
            df_grid_inventory$nearbyItemsPanel.setBounds(leftPos + imageWidth + 8, topPos + 8);
            df_grid_inventory$nearbyItemsPanel.render(graphics, mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$clickNearbyItems(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()
                && df_grid_inventory$nearbyItemsPanel.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$releaseNearbyItems(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()
                && df_grid_inventory$nearbyItemsPanel.mouseReleased(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

}
