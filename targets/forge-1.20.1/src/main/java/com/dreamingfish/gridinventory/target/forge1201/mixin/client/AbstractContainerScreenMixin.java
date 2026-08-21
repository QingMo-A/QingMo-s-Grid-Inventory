package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import com.dreamingfish.gridinventory.client.screen.panel.ExternalContainerSidebar;
import com.dreamingfish.gridinventory.client.access.ExternalContainerSidebarAccess;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen
        implements ExternalContainerSidebarAccess {
    @Shadow protected T menu;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    @Shadow @Nullable protected Slot hoveredSlot;

    @Unique
    private final ExternalContainerSidebar df_grid_inventory$externalSidebar = new ExternalContainerSidebar();

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void df_grid_inventory$initExternalSidebar(CallbackInfo ci) {
        df_grid_inventory$externalSidebar.init((Screen) (Object) this, menu, width, height, imageWidth, imageHeight);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void df_grid_inventory$renderExternalSidebar(GuiGraphics graphics, int mouseX, int mouseY,
                                                         float partialTick, CallbackInfo ci) {
        df_grid_inventory$externalSidebar.render(graphics, menu, hoveredSlot, leftPos, topPos, mouseX, mouseY);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$suppressTooltipWhileDragging(GuiGraphics graphics, int mouseX, int mouseY,
                                                                CallbackInfo ci) {
        if (df_grid_inventory$externalSidebar.isDraggingSidebarItem()
                || df_grid_inventory$externalSidebar.contains(mouseX, mouseY)) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$externalSidebarClick(double mouseX, double mouseY, int button,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (df_grid_inventory$externalSidebar.mouseClicked(menu, mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$externalSidebarRelease(double mouseX, double mouseY, int button,
                                                          CallbackInfoReturnable<Boolean> cir) {
        if (df_grid_inventory$externalSidebar.mouseReleased(menu, hoveredSlot, mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$externalSidebarDrag(double mouseX, double mouseY, int button,
                                                       double dragX, double dragY,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (df_grid_inventory$externalSidebar.mouseDragged(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public ExternalContainerSidebar df_grid_inventory$externalSidebar() {
        return df_grid_inventory$externalSidebar;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return df_grid_inventory$externalSidebar.mouseScrolled(mouseX, mouseY, delta)
                || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$externalSidebarKey(int keyCode, int scanCode, int modifiers,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (df_grid_inventory$externalSidebar.keyPressed(menu, keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }
}
