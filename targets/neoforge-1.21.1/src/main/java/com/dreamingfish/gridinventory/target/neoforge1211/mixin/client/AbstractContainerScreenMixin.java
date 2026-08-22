package com.dreamingfish.gridinventory.target.neoforge1211.mixin.client;

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

import java.util.IdentityHashMap;
import java.util.Map;

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
    @Unique
    private final Map<Slot, int[]> df_grid_inventory$originalPlayerSlotPositions = new IdentityHashMap<>();
    @Unique
    private int df_grid_inventory$originalImageHeight = -1;
    @Unique
    private int df_grid_inventory$originalInventoryLabelY;
    @Unique
    private boolean df_grid_inventory$layoutApplied;
    @Unique
    private boolean df_grid_inventory$nativeSideWidgetsArranged;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void df_grid_inventory$prepareVanillaContainerLayout(CallbackInfo ci) {
        df_grid_inventory$restoreContainerLayout();
        df_grid_inventory$nativeSideWidgetsArranged = false;
        df_grid_inventory$bottomCapTexture = null;
        Screen screen = (Screen) (Object) this;
        if (!ExternalContainerSidebar.supports(screen, menu)) {
            df_grid_inventory$vanillaLayout = VanillaContainerScreenLayout.none();
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            df_grid_inventory$vanillaLayout = VanillaContainerScreenLayout.none();
            return;
        }
        Inventory playerInventory = minecraft.player.getInventory();
        df_grid_inventory$vanillaLayout = VanillaContainerScreenLayout.resolve(
                screen, menu, playerInventory, imageHeight);
        if (!df_grid_inventory$vanillaLayout.hidesPlayerInventory()) {
            return;
        }

        VanillaContainerScreenLayout.BottomCap bottomCap = df_grid_inventory$vanillaLayout.bottomCap();
        if (bottomCap != null) {
            df_grid_inventory$bottomCapTexture = ResourceLocation.withDefaultNamespace(bottomCap.texturePath());
        }
        if (df_grid_inventory$vanillaLayout.compact()) {
            df_grid_inventory$originalImageHeight = imageHeight;
            imageHeight = df_grid_inventory$vanillaLayout.compactImageHeight();
        }
        df_grid_inventory$originalInventoryLabelY = inventoryLabelY;
        df_grid_inventory$layoutApplied = true;
        inventoryLabelY = VanillaContainerScreenLayout.HIDDEN_SLOT_COORDINATE;
        for (Slot slot : menu.slots) {
            if (slot.container == playerInventory) {
                df_grid_inventory$originalPlayerSlotPositions.put(slot, new int[]{slot.x, slot.y});
                SlotPositionAccessor position = (SlotPositionAccessor) slot;
                position.df_grid_inventory$setX(VanillaContainerScreenLayout.HIDDEN_SLOT_COORDINATE);
                position.df_grid_inventory$setY(VanillaContainerScreenLayout.HIDDEN_SLOT_COORDINATE);
            }
        }
    }

    @Unique
    private void df_grid_inventory$restoreContainerLayout() {
        if (df_grid_inventory$originalImageHeight >= 0) {
            imageHeight = df_grid_inventory$originalImageHeight;
            df_grid_inventory$originalImageHeight = -1;
        }
        if (df_grid_inventory$layoutApplied) {
            inventoryLabelY = df_grid_inventory$originalInventoryLabelY;
            df_grid_inventory$layoutApplied = false;
        }
        if (!df_grid_inventory$originalPlayerSlotPositions.isEmpty()) {
            for (Map.Entry<Slot, int[]> entry : df_grid_inventory$originalPlayerSlotPositions.entrySet()) {
                SlotPositionAccessor position = (SlotPositionAccessor) entry.getKey();
                position.df_grid_inventory$setX(entry.getValue()[0]);
                position.df_grid_inventory$setY(entry.getValue()[1]);
            }
            df_grid_inventory$originalPlayerSlotPositions.clear();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void df_grid_inventory$initExternalSidebar(CallbackInfo ci) {
        df_grid_inventory$externalSidebar.init((Screen) (Object) this, menu, width, height, imageWidth, imageHeight);
    }

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void df_grid_inventory$renderVanillaContainerDecoration(GuiGraphics graphics, int mouseX, int mouseY,
                                                                     float partialTick, CallbackInfo ci) {
        if (df_grid_inventory$vanillaLayout.clipsBackground()) {
            graphics.disableScissor();
        }
        df_grid_inventory$renderVanillaContainerDecoration(graphics);
    }

    @Inject(
            method = "renderBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"
            )
    )
    private void df_grid_inventory$clipVanillaContainerBody(GuiGraphics graphics, int mouseX, int mouseY,
                                                             float partialTick, CallbackInfo ci) {
        if (df_grid_inventory$vanillaLayout.clipsBackground()) {
            graphics.enableScissor(leftPos, topPos,
                    leftPos + imageWidth, topPos + df_grid_inventory$vanillaLayout.bodyHeight());
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void df_grid_inventory$renderExternalSidebar(GuiGraphics graphics, int mouseX, int mouseY,
                                                         float partialTick, CallbackInfo ci) {
        df_grid_inventory$externalSidebar.render(graphics, menu, hoveredSlot, leftPos, topPos, mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void df_grid_inventory$arrangeNativeSideWidgets(GuiGraphics graphics, int mouseX, int mouseY,
                                                             float partialTick, CallbackInfo ci) {
        if (!df_grid_inventory$nativeSideWidgetsArranged
                && df_grid_inventory$vanillaLayout.nativeBackgroundResize()) {
            VanillaContainerScreenLayout.arrangeNativeSideWidgets(
                    children(), leftPos, topPos, imageWidth, imageHeight, height);
            df_grid_inventory$nativeSideWidgetsArranged = true;
        }
    }

    @Unique
    private void df_grid_inventory$renderVanillaContainerDecoration(GuiGraphics graphics) {
        if (df_grid_inventory$vanillaLayout.clipsBackground()) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return df_grid_inventory$externalSidebar.mouseScrolled(mouseX, mouseY, deltaY)
                || super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$externalSidebarKey(int keyCode, int scanCode, int modifiers,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if (df_grid_inventory$externalSidebar.keyPressed(menu, keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }
}
