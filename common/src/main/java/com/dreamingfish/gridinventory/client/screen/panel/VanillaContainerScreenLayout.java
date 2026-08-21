package com.dreamingfish.gridinventory.client.screen.panel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public final class VanillaContainerScreenLayout {
    public static final int HIDDEN_SLOT_COORDINATE = -10_000;
    public static final int BOTTOM_CAP_HEIGHT = 7;
    public static final int STANDARD_BOTTOM_CAP_SOURCE_Y = 159;
    public static final int STANDARD_IMAGE_WIDTH = 176;
    public static final int RECIPE_BOOK_WIDTH = 147;
    public static final int RECIPE_BOOK_HEIGHT = 166;

    private static final int RECIPE_BOOK_WIDE_OFFSET_X = 86;

    private static final int STANDARD_BODY_HEIGHT = 83;
    private static final int HOPPER_BODY_HEIGHT = 50;
    private static final int BEACON_BODY_HEIGHT = 136;
    private static final int CAP_LEFT_WIDTH = 7;
    private static final int CAP_OVERLAY_OFFSET_Y = 1;

    private static final Layout NONE = new Layout(false, 0, false, null, null);
    private static final Layout RECIPE_BOOK = compact(STANDARD_BODY_HEIGHT, true, null);
    private static final Layout BREWING_STAND = standard("textures/gui/container/brewing_stand.png");
    private static final Layout CARTOGRAPHY_TABLE = standard("textures/gui/container/cartography_table.png");
    private static final Layout CRAFTER = standard("textures/gui/container/crafter.png");
    private static final Layout DISPENSER = standard("textures/gui/container/dispenser.png");
    private static final Layout ENCHANTMENT = standard("textures/gui/container/enchanting_table.png");
    private static final Layout GRINDSTONE = standard("textures/gui/container/grindstone.png");
    private static final Layout HORSE = standard("textures/gui/container/horse.png");
    private static final Layout ANVIL = standard("textures/gui/container/anvil.png");
    private static final Layout SMITHING = standard("textures/gui/container/smithing.png");
    private static final Layout LOOM = standard("textures/gui/container/loom.png");
    private static final Layout SHULKER = compact(STANDARD_BODY_HEIGHT, false,
            new BottomCap("textures/gui/container/shulker_box.png", 160, STANDARD_IMAGE_WIDTH));
    private static final Layout STONECUTTER = standard("textures/gui/container/stonecutter.png");
    private static final Layout HOPPER = compact(HOPPER_BODY_HEIGHT, false,
            new BottomCap("textures/gui/container/hopper.png", 126, STANDARD_IMAGE_WIDTH));
    private static final Layout BEACON = compact(BEACON_BODY_HEIGHT, false,
            new BottomCap("textures/gui/container/beacon.png", 212, 230));
    private static final Layout MERCHANT = new Layout(true, 0, false, null,
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
            case "net.minecraft.client.gui.screens.inventory.BrewingStandScreen" -> BREWING_STAND;
            case "net.minecraft.client.gui.screens.inventory.CartographyTableScreen" -> CARTOGRAPHY_TABLE;
            case "net.minecraft.client.gui.screens.inventory.CrafterScreen" -> CRAFTER;
            case "net.minecraft.client.gui.screens.inventory.DispenserScreen" -> DISPENSER;
            case "net.minecraft.client.gui.screens.inventory.EnchantmentScreen" -> ENCHANTMENT;
            case "net.minecraft.client.gui.screens.inventory.GrindstoneScreen" -> GRINDSTONE;
            case "net.minecraft.client.gui.screens.inventory.HorseInventoryScreen" -> HORSE;
            case "net.minecraft.client.gui.screens.inventory.AnvilScreen" -> ANVIL;
            case "net.minecraft.client.gui.screens.inventory.SmithingScreen" -> SMITHING;
            case "net.minecraft.client.gui.screens.inventory.LoomScreen" -> LOOM;
            case "net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen" -> SHULKER;
            case "net.minecraft.client.gui.screens.inventory.StonecutterScreen" -> STONECUTTER;
            case "net.minecraft.client.gui.screens.inventory.HopperScreen" -> HOPPER;
            case "net.minecraft.client.gui.screens.inventory.BeaconScreen" -> BEACON;
            case "net.minecraft.client.gui.screens.inventory.MerchantScreen" -> MERCHANT;
            default -> NONE;
        };
    }

    public static void renderBottomCap(GuiGraphics graphics, ResourceLocation texture,
                                       int sourceY, int sourceWidth,
                                       int x, int y, int width) {
        int targetY = y + CAP_OVERLAY_OFFSET_Y;
        graphics.blit(texture, x, targetY, 0, sourceY,
                CAP_LEFT_WIDTH, BOTTOM_CAP_HEIGHT);
        int remaining = Math.max(0, width - CAP_LEFT_WIDTH * 2);
        int targetX = x + CAP_LEFT_WIDTH;
        int interiorWidth = sourceWidth - CAP_LEFT_WIDTH * 2;
        while (remaining > 0) {
            int segmentWidth = Math.min(interiorWidth, remaining);
            graphics.blit(texture, targetX, targetY, CAP_LEFT_WIDTH, sourceY,
                    segmentWidth, BOTTOM_CAP_HEIGHT);
            targetX += segmentWidth;
            remaining -= segmentWidth;
        }
        graphics.blit(texture, x + width - CAP_LEFT_WIDTH, targetY, sourceWidth - CAP_LEFT_WIDTH,
                sourceY, CAP_LEFT_WIDTH, BOTTOM_CAP_HEIGHT);
    }

    public static int alignedRecipeBookScreenHeight(int screenHeight, int imageHeight) {
        if (imageHeight >= RECIPE_BOOK_HEIGHT) {
            return screenHeight;
        }
        return RECIPE_BOOK_HEIGHT + alignedRecipeBookTop(screenHeight, imageHeight) * 2;
    }

    public static boolean isInsideAlignedRecipeBook(double mouseX, double mouseY,
                                                    int screenWidth, int screenHeight, int imageHeight,
                                                    boolean widthTooNarrow) {
        int recipeLeft = (screenWidth - RECIPE_BOOK_WIDTH) / 2
                - (widthTooNarrow ? 0 : RECIPE_BOOK_WIDE_OFFSET_X);
        int recipeTop = alignedRecipeBookTop(screenHeight, imageHeight);
        return mouseX >= recipeLeft && mouseY >= recipeTop
                && mouseX < recipeLeft + RECIPE_BOOK_WIDTH
                && mouseY < recipeTop + RECIPE_BOOK_HEIGHT;
    }

    private static int alignedRecipeBookTop(int screenHeight, int imageHeight) {
        if (imageHeight >= RECIPE_BOOK_HEIGHT) {
            return Math.max(0, (screenHeight - RECIPE_BOOK_HEIGHT) / 2);
        }
        int menuTop = Math.max(0, (screenHeight - imageHeight) / 2);
        return Math.min(menuTop, Math.max(0, screenHeight - RECIPE_BOOK_HEIGHT));
    }

    private static Layout standard(String texturePath) {
        return compact(STANDARD_BODY_HEIGHT, false,
                new BottomCap(texturePath, STANDARD_BOTTOM_CAP_SOURCE_Y, STANDARD_IMAGE_WIDTH));
    }

    private static Layout compact(int bodyHeight, boolean decorateInRenderBg, BottomCap bottomCap) {
        return new Layout(true, bodyHeight, decorateInRenderBg, bottomCap, null);
    }

    public record Layout(boolean hidesPlayerInventory, int bodyHeight,
                         boolean decorateInRenderBg, BottomCap bottomCap, Mask mask) {
        public boolean compact() {
            return bodyHeight > 0;
        }

        public int compactImageHeight() {
            return bodyHeight + BOTTOM_CAP_HEIGHT;
        }
    }

    public record BottomCap(String texturePath, int sourceY, int sourceWidth) {
    }

    public record Mask(int x, int y, int width, int height, int color) {
    }
}
