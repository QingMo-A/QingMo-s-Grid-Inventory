package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import com.dreamingfish.gridinventory.client.access.ExternalContainerSidebarAccess;
import com.dreamingfish.gridinventory.client.access.SlotPositionAccessor;
import com.dreamingfish.gridinventory.client.screen.panel.ExternalContainerSidebar;
import com.dreamingfish.gridinventory.client.screen.panel.VanillaContainerScreenLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
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
    @Shadow protected int inventoryLabelY;
    @Shadow @Nullable protected Slot hoveredSlot;

    @Unique
    private final ExternalContainerSidebar df_grid_inventory$externalSidebar = new ExternalContainerSidebar();
    @Unique
    private VanillaContainerScreenLayout.Layout df_grid_inventory$vanillaLayout =
            VanillaContainerScreenLayout.none();
    @Unique @Nullable
    private ResourceLocation df_grid_inventory$bottomCapTexture;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void df_grid_inventory$prepareVanillaContainerLayout(CallbackInfo ci) {
        df_grid_inventory$bottomCapTexture = null;
        Screen screen = (Screen) (Object) this;
        df_grid_inventory$vanillaLayout = VanillaContainerScreenLayout.resolve(screen);
        if (!df_grid_inventory$vanillaLayout.hidesPlayerInventory()
                || !ExternalContainerSidebar.supports(screen, menu)) {
            df_grid_inventory$vanillaLayout = VanillaContainerScreenLayout.none();
            return;
        }

        VanillaContainerScreenLayout.BottomCap bottomCap = df_grid_inventory$vanillaLayout.bottomCap();
        if (bottomCap != null) {
            df_grid_inventory$bottomCapTexture = new ResourceLocation("minecraft", bottomCap.texturePath());
        }
        if (df_grid_inventory$vanillaLayout.compact()) {
            imageHeight = df_grid_inventory$vanillaLayout.compactImageHeight();
        }
        inventoryLabelY = VanillaContainerScreenLayout.HIDDEN_SLOT_COORDINATE;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Inventory playerInventory = minecraft.player.getInventory();
        for (Slot slot : menu.slots) {
            if (slot.container == playerInventory) {
                SlotPositionAccessor position = (SlotPositionAccessor) slot;
                position.df_grid_inventory$setX(VanillaContainerScreenLayout.HIDDEN_SLOT_COORDINATE);
                position.df_grid_inventory$setY(VanillaContainerScreenLayout.HIDDEN_SLOT_COORDINATE);
            }
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void df_grid_inventory$initExternalSidebar(CallbackInfo ci) {
        df_grid_inventory$externalSidebar.init((Screen) (Object) this, menu, width, height, imageWidth, imageHeight);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"
            )
    )
    private void df_grid_inventory$clipVanillaContainerBody(GuiGraphics graphics, int mouseX, int mouseY,
                                                             float partialTick, CallbackInfo ci) {
        if (df_grid_inventory$vanillaLayout.compact()
                && !df_grid_inventory$vanillaLayout.decorateInRenderBg()) {
            graphics.enableScissor(leftPos, topPos,
                    leftPos + imageWidth, topPos + df_grid_inventory$vanillaLayout.bodyHeight());
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void df_grid_inventory$renderVanillaContainerDecoration(GuiGraphics graphics, int mouseX, int mouseY,
                                                                     float partialTick, CallbackInfo ci) {
        if (df_grid_inventory$vanillaLayout.compact()
                && !df_grid_inventory$vanillaLayout.decorateInRenderBg()) {
            graphics.disableScissor();
        }
        df_grid_inventory$renderVanillaContainerDecoration(graphics);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void df_grid_inventory$renderExternalSidebar(GuiGraphics graphics, int mouseX, int mouseY,
                                                         float partialTick, CallbackInfo ci) {
        df_grid_inventory$externalSidebar.render(graphics, menu, hoveredSlot, leftPos, topPos, mouseX, mouseY);
    }

    @Unique
    private void df_grid_inventory$renderVanillaContainerDecoration(GuiGraphics graphics) {
        if (df_grid_inventory$vanillaLayout.compact()
                && !df_grid_inventory$vanillaLayout.decorateInRenderBg()) {
            VanillaContainerScreenLayout.BottomCap bottomCap = df_grid_inventory$vanillaLayout.bottomCap();
            if (bottomCap != null && df_grid_inventory$bottomCapTexture != null) {
                VanillaContainerScreenLayout.renderBottomCap(graphics, df_grid_inventory$bottomCapTexture,
                        bottomCap.sourceY(), bottomCap.sourceWidth(),
                        leftPos, topPos + df_grid_inventory$vanillaLayout.bodyHeight(), imageWidth);
            }
            return;
        }
        VanillaContainerScreenLayout.Mask mask = df_grid_inventory$vanillaLayout.mask();
        if (mask != null) {
            graphics.fill(leftPos + mask.x(), topPos + mask.y(),
                    leftPos + mask.x() + mask.width(), topPos + mask.y() + mask.height(), mask.color());
        }
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
