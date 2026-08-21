package com.dreamingfish.gridinventory.client.screen.panel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public final class VanillaContainerScreenLayout {
    public static final int HIDDEN_SLOT_COORDINATE = -10_000;
    public static final int BOTTOM_CAP_SOURCE_Y = 215;
    public static final int BOTTOM_CAP_HEIGHT = 7;

    private static final int STANDARD_BODY_HEIGHT = 83;
    private static final int HOPPER_BODY_HEIGHT = 50;
    private static final int BEACON_BODY_HEIGHT = 136;
    private static final int CAP_LEFT_WIDTH = 7;
    private static final int CAP_INTERIOR_WIDTH = 162;
    private static final int CAP_RIGHT_SOURCE_X = 169;

    private static final Layout NONE = new Layout(false, 0, false, null);
    private static final Layout STANDARD = compact(STANDARD_BODY_HEIGHT, false);
    private static final Layout RECIPE_BOOK = compact(STANDARD_BODY_HEIGHT, true);
    private static final Layout HOPPER = compact(HOPPER_BODY_HEIGHT, false);
    private static final Layout BEACON = compact(BEACON_BODY_HEIGHT, false);
    private static final Layout MERCHANT = new Layout(true, 0, false,
            new Mask(101, 75, 171, 88, 0xFFC6C6C6));

    private VanillaContainerScreenLayout() {
    }

    public static Layout none() {
        return NONE;
    }

    public static Layout resolve(Screen screen) {
        return switch (screen.getClass().getName()) {
            case "net.minecraft.client.gui.screens.inventory.CraftingScreen",
                 "net.minecraft.client.gui.screens.inventory.FurnaceScreen",
                 "net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen",
                 "net.minecraft.client.gui.screens.inventory.SmokerScreen" -> RECIPE_BOOK;
            case "net.minecraft.client.gui.screens.inventory.BrewingStandScreen",
                 "net.minecraft.client.gui.screens.inventory.CartographyTableScreen",
                 "net.minecraft.client.gui.screens.inventory.CrafterScreen",
                 "net.minecraft.client.gui.screens.inventory.DispenserScreen",
                 "net.minecraft.client.gui.screens.inventory.EnchantmentScreen",
                 "net.minecraft.client.gui.screens.inventory.GrindstoneScreen",
                 "net.minecraft.client.gui.screens.inventory.HorseInventoryScreen",
                 "net.minecraft.client.gui.screens.inventory.AnvilScreen",
                 "net.minecraft.client.gui.screens.inventory.SmithingScreen",
                 "net.minecraft.client.gui.screens.inventory.LoomScreen",
                 "net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen",
                 "net.minecraft.client.gui.screens.inventory.StonecutterScreen" -> STANDARD;
            case "net.minecraft.client.gui.screens.inventory.HopperScreen" -> HOPPER;
            case "net.minecraft.client.gui.screens.inventory.BeaconScreen" -> BEACON;
            case "net.minecraft.client.gui.screens.inventory.MerchantScreen" -> MERCHANT;
            default -> NONE;
        };
    }

    public static void renderBottomCap(GuiGraphics graphics, ResourceLocation texture,
                                       int x, int y, int width) {
        graphics.blit(texture, x, y, 0, BOTTOM_CAP_SOURCE_Y,
                CAP_LEFT_WIDTH, BOTTOM_CAP_HEIGHT);
        int remaining = Math.max(0, width - CAP_LEFT_WIDTH * 2);
        int targetX = x + CAP_LEFT_WIDTH;
        while (remaining > 0) {
            int segmentWidth = Math.min(CAP_INTERIOR_WIDTH, remaining);
            graphics.blit(texture, targetX, y, CAP_LEFT_WIDTH, BOTTOM_CAP_SOURCE_Y,
                    segmentWidth, BOTTOM_CAP_HEIGHT);
            targetX += segmentWidth;
            remaining -= segmentWidth;
        }
        graphics.blit(texture, x + width - CAP_LEFT_WIDTH, y, CAP_RIGHT_SOURCE_X,
                BOTTOM_CAP_SOURCE_Y, CAP_LEFT_WIDTH, BOTTOM_CAP_HEIGHT);
    }

    private static Layout compact(int bodyHeight, boolean decorateInRenderBg) {
        return new Layout(true, bodyHeight, decorateInRenderBg, null);
    }

    public record Layout(boolean hidesPlayerInventory, int bodyHeight,
                         boolean decorateInRenderBg, Mask mask) {
        public boolean compact() {
            return bodyHeight > 0;
        }

        public int compactImageHeight() {
            return bodyHeight + BOTTOM_CAP_HEIGHT;
        }
    }

    public record Mask(int x, int y, int width, int height, int color) {
    }
}
