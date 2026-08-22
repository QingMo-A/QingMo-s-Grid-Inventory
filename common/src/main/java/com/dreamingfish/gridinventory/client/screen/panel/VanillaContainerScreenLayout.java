package com.dreamingfish.gridinventory.client.screen.panel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final int SLOT_SIZE = 18;
    private static final int MIN_PLAYER_ROW_SIZE = 9;
    private static final int MIN_ADAPTIVE_BODY_HEIGHT = 32;
    private static final int MIN_ADAPTIVE_HEIGHT_SAVING = 18;
    private static final int SIDE_WIDGET_GAP = 1;
    private static final int SCREEN_EDGE_PADDING = 2;

    // One entry covers every screen implemented on top of the same resizable GUI framework.
    private static final Set<String> NATIVE_RESIZABLE_SCREEN_BASES = Set.of(
            "mekanism.client.gui.GuiMekanism"
    );

    private static final Layout NONE = new Layout(false, 0, false, false, null, null);
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
    private static final Layout MERCHANT = new Layout(true, 0, false, false, null,
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

    public static Layout resolve(Screen screen, AbstractContainerMenu menu,
                                 Inventory playerInventory, int originalImageHeight) {
        Layout vanilla = resolve(screen);
        if (vanilla != NONE) {
            return vanilla;
        }
        if (!hasNativeResizableBackground(screen.getClass())) {
            return NONE;
        }
        return resolveAdaptive(menu, playerInventory, originalImageHeight);
    }

    public static void renderBottomCap(GuiGraphics graphics, ResourceLocation texture,
                                       int sourceY, int sourceWidth,
                                       int x, int y, int width) {
        graphics.blit(texture, x, y, 0, sourceY,
                CAP_LEFT_WIDTH, BOTTOM_CAP_HEIGHT);
        int remaining = Math.max(0, width - CAP_LEFT_WIDTH * 2);
        int targetX = x + CAP_LEFT_WIDTH;
        int interiorWidth = sourceWidth - CAP_LEFT_WIDTH * 2;
        while (remaining > 0) {
            int segmentWidth = Math.min(interiorWidth, remaining);
            graphics.blit(texture, targetX, y, CAP_LEFT_WIDTH, sourceY,
                    segmentWidth, BOTTOM_CAP_HEIGHT);
            targetX += segmentWidth;
            remaining -= segmentWidth;
        }
        graphics.blit(texture, x + width - CAP_LEFT_WIDTH, y, sourceWidth - CAP_LEFT_WIDTH,
                sourceY, CAP_LEFT_WIDTH, BOTTOM_CAP_HEIGHT);
    }

    public static void arrangeNativeSideWidgets(List<? extends GuiEventListener> children,
                                                int menuLeft, int menuTop,
                                                int imageWidth, int imageHeight, int screenHeight) {
        List<AbstractWidget> left = children.stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(VanillaContainerScreenLayout::isNativeFrameworkElement)
                .filter(widget -> widget.getX() + widget.getWidth() <= menuLeft + 1)
                .sorted((first, second) -> Integer.compare(first.getY(), second.getY()))
                .toList();
        List<AbstractWidget> right = children.stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(VanillaContainerScreenLayout::isNativeFrameworkElement)
                .filter(widget -> widget.getX() >= menuLeft + imageWidth - 1)
                .sorted((first, second) -> Integer.compare(first.getY(), second.getY()))
                .toList();
        arrangeSideWidgetColumn(left, menuTop, imageHeight, screenHeight);
        arrangeSideWidgetColumn(right, menuTop, imageHeight, screenHeight);
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
        return new Layout(true, bodyHeight, decorateInRenderBg, false, bottomCap, null);
    }

    public record Layout(boolean hidesPlayerInventory, int bodyHeight,
                         boolean decorateInRenderBg, boolean nativeBackgroundResize,
                         BottomCap bottomCap, Mask mask) {
        public boolean compact() {
            return bodyHeight > 0;
        }

        public int compactImageHeight() {
            return bodyHeight + BOTTOM_CAP_HEIGHT;
        }

        public boolean clipsBackground() {
            return compact() && !decorateInRenderBg && !nativeBackgroundResize;
        }
    }

    public record BottomCap(String texturePath, int sourceY, int sourceWidth) {
    }

    public record Mask(int x, int y, int width, int height, int color) {
    }

    private static Layout resolveAdaptive(AbstractContainerMenu menu, Inventory playerInventory,
                                          int originalImageHeight) {
        Map<Integer, Set<Integer>> playerRows = new HashMap<>();
        int operationBottom = 0;
        for (Slot slot : menu.slots) {
            if (slot.x < 0 || slot.y < 0) {
                continue;
            }
            if (slot.container == playerInventory) {
                playerRows.computeIfAbsent(slot.y, ignored -> new HashSet<>()).add(slot.x);
            } else {
                operationBottom = Math.max(operationBottom, slot.y + SLOT_SIZE);
            }
        }

        int firstPlayerRowY = playerRows.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MIN_PLAYER_ROW_SIZE)
                .mapToInt(Map.Entry::getKey)
                .min()
                .orElse(Integer.MAX_VALUE);
        if (firstPlayerRowY == Integer.MAX_VALUE || operationBottom > firstPlayerRowY) {
            return NONE;
        }

        int bodyHeight = firstPlayerRowY - 1;
        int compactImageHeight = bodyHeight + BOTTOM_CAP_HEIGHT;
        if (bodyHeight < MIN_ADAPTIVE_BODY_HEIGHT
                || originalImageHeight - compactImageHeight < MIN_ADAPTIVE_HEIGHT_SAVING) {
            return NONE;
        }
        return new Layout(true, bodyHeight, false, true, null, null);
    }

    private static boolean hasNativeResizableBackground(Class<?> screenClass) {
        for (Class<?> type = screenClass; type != null; type = type.getSuperclass()) {
            if (NATIVE_RESIZABLE_SCREEN_BASES.contains(type.getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNativeFrameworkElement(AbstractWidget widget) {
        return widget.getClass().getName().startsWith("mekanism.client.gui.element.");
    }

    private static void arrangeSideWidgetColumn(List<AbstractWidget> widgets,
                                                int menuTop, int imageHeight, int screenHeight) {
        if (widgets.isEmpty()) {
            return;
        }
        int totalHeight = widgets.stream().mapToInt(AbstractWidget::getHeight).sum()
                + SIDE_WIDGET_GAP * (widgets.size() - 1);
        int desiredTop = menuTop + (imageHeight - totalHeight) / 2;
        int maximumTop = Math.max(SCREEN_EDGE_PADDING, screenHeight - totalHeight - SCREEN_EDGE_PADDING);
        int targetY = Math.max(SCREEN_EDGE_PADDING, Math.min(desiredTop, maximumTop));
        for (AbstractWidget widget : widgets) {
            moveNativeFrameworkElement(widget, targetY - widget.getY());
            targetY += widget.getHeight() + SIDE_WIDGET_GAP;
        }
    }

    private static void moveNativeFrameworkElement(AbstractWidget widget, int deltaY) {
        if (deltaY == 0) {
            return;
        }
        try {
            widget.getClass().getMethod("move", int.class, int.class).invoke(widget, 0, deltaY);
        } catch (ReflectiveOperationException ignored) {
            widget.setY(widget.getY() + deltaY);
        }
    }
}
