package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.creative.VanillaCreativeTabView;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class VanillaCreativeSidebarPanel {
    private static final int SLOT = 18;
    private static final int TAB = 20;
    private static final int PAD = 6;
    private static final int TITLE_HEIGHT = 14;
    private static final int TAB_BAR_HEIGHT = 24;
    private static final int SEARCH_HEIGHT = 16;
    private static final int SEARCH_GAP = 4;
    private int left;
    private int top;
    private int width;
    private int height;
    private final PanelScrollbar itemScrollbar = new PanelScrollbar();
    private int tabScroll;
    private double tabScrollPosition;
    private double tabScrollVelocity;
    private float tabScrollbarAlpha;
    private boolean draggingTabScrollbar;
    private int tabDragOffset;
    private int itemScrollPixels;
    private int selectedTab;
    private String search = "";
    private boolean searchFocused;
    private CreativeItemReference draggedItem;

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        selectedTab = Math.min(selectedTab, Math.max(0, tabs().size() - 1));
        tabScroll = clampTabScroll(tabScroll);
        tabScrollPosition = clampTabScroll((int) Math.round(tabScrollPosition));
    }

    public void selectDefaultTab() {
        List<VanillaCreativeTabView> tabs = tabs();
        GridInventoryServices.creativeTabs().selectedDefaultTab().ifPresent(defaultTab -> {
            int index = tabs.indexOf(defaultTab);
            if (index >= 0) {
                selectedTab = index;
            }
        });
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, boolean suppressTooltip) {
        Font font = Minecraft.getInstance().font;
        graphics.fill(left, top, left + width, top + height, 0xEE171717);
        graphics.drawString(font, Component.translatable("screen.df_grid_inventory.creative_items"), left + PAD, top + 4, 0xFFFFFF, false);
        renderTabs(graphics, mouseX, mouseY);
        renderSearch(graphics, font);
        int gridLeft = gridLeft();
        int gridTop = gridTop();
        int columns = columns();
        int rows = visibleRows();
        List<CreativeItemReference> items = visibleItems();
        itemScrollbar.update(gridLeft, gridTop, columns * SLOT + 8, rows * SLOT, totalRows(items.size(), columns) * SLOT);
        itemScrollPixels = itemScrollbar.scroll();
        int firstRow = itemScrollPixels / SLOT;
        int rowOffset = itemScrollPixels % SLOT;
        int hoveredIndex = hoveredSlot(mouseX, mouseY).orElse(-1);
        graphics.enableScissor(gridLeft, gridTop, gridLeft + columns * SLOT, gridTop + rows * SLOT);
        for (int index = firstRow * columns; index < items.size(); index++) {
            int local = index - firstRow * columns;
            int row = local / columns;
            if (row > rows) {
                break;
            }
            int col = local % columns;
            int x = gridLeft + col * SLOT;
            int y = gridTop + row * SLOT - rowOffset;
            ItemStack stack = items.get(index).stack();
            graphics.fill(x, y, x + SLOT, y + SLOT, hoveredIndex == index ? 0x665A6F9A : 0x55333333);
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }
        graphics.disableScissor();
        itemScrollbar.render(graphics, mouseX, mouseY);
        if (!suppressTooltip) {
            hoveredTab(mouseX, mouseY).ifPresent(tab -> graphics.renderTooltip(font, tab.title(), mouseX, mouseY));
            hoveredSlot(mouseX, mouseY).ifPresent(index -> graphics.renderTooltip(font, items.get(index).stack(), mouseX, mouseY));
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !contains(mouseX, mouseY)) {
            searchFocused = false;
            return false;
        }
        if (itemScrollbar.mouseClicked(mouseX, mouseY, button) || mouseClickedTabScrollbar(mouseX, mouseY, button)) {
            searchFocused = false;
            return true;
        }
        Optional<Integer> tab = hoveredTabIndex((int) mouseX, (int) mouseY);
        if (tab.isPresent()) {
            selectedTab = tab.get();
            tabScroll = clampTabScroll(tabScroll);
            resetItemScroll();
            searchFocused = false;
            return true;
        }
        if (inSearch(mouseX, mouseY)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;
        Optional<Integer> slot = hoveredSlot((int) mouseX, (int) mouseY);
        if (slot.isPresent()) {
            draggedItem = visibleItems().get(slot.get());
            return true;
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (itemScrollbar.mouseDragged(mouseX, mouseY, button) || mouseDraggedTabScrollbar(mouseX, mouseY, button)) {
            return true;
        }
        return button == 0 && draggedItem != null;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean releasedScrollbar = itemScrollbar.mouseReleased(button) | mouseReleasedTabScrollbar(button);
        if (releasedScrollbar) {
            return true;
        }
        return button == 0 && draggedItem != null;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        if (inTabBar(mouseX, mouseY)) {
            scrollTabs(deltaY);
            return true;
        }
        return itemScrollbar.mouseScrolled(mouseX, mouseY, deltaY, SLOT * 3);
    }

    public boolean keyPressed(int keyCode) {
        if (!searchFocused) {
            return false;
        }
        if (keyCode == 256) {
            searchFocused = false;
            return true;
        }
        if (keyCode == 259 && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            resetItemScroll();
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint) {
        if (!searchFocused || Character.isISOControl(codePoint)) {
            return false;
        }
        search += codePoint;
        resetItemScroll();
        return true;
    }

    public Optional<ItemStack> draggedStack() {
        return draggedItem().map(CreativeItemReference::stack);
    }

    public Optional<CreativeItemReference> draggedItem() {
        return Optional.ofNullable(draggedItem);
    }

    public void clearDrag() {
        draggedItem = null;
    }

    public boolean isSearchFocused() {
        return searchFocused;
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        List<VanillaCreativeTabView> tabs = tabs();
        int x = left + PAD;
        int y = tabTop();
        int visible = visibleTabCount();
        advanceTabScroll();
        int firstTab = (int) Math.floor(tabScrollPosition);
        int tabOffset = (int) Math.round((tabScrollPosition - firstTab) * TAB);
        graphics.enableScissor(x, y, x + visible * TAB, y + 18);
        for (int index = firstTab; index < tabs.size(); index++) {
            int local = index - firstTab;
            int tx = x + local * TAB - tabOffset;
            if (local > visible) {
                break;
            }
            int color = index == selectedTab ? 0xFF505A70 : 0xFF2B303A;
            graphics.fill(tx, y, tx + 18, y + 18, color);
            graphics.renderItem(tabs.get(index).icon(), tx + 1, y + 1);
        }
        graphics.disableScissor();
        renderTabScrollbar(graphics, mouseX, mouseY, x, y, visible, tabs.size());
    }

    private void renderSearch(GuiGraphics graphics, Font font) {
        int color = searchFocused ? 0xFF8FB3FF : 0xFF606A7A;
        int boxLeft = left + PAD;
        int boxTop = searchTop();
        int boxRight = left + width - PAD;
        graphics.fill(boxLeft, boxTop, boxRight, boxTop + SEARCH_HEIGHT, 0xFF111827);
        graphics.renderOutline(boxLeft, boxTop, boxRight - boxLeft, SEARCH_HEIGHT, color);
        Component text = search.isEmpty()
                ? Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(search);
        graphics.drawString(font, text, boxLeft + 4, boxTop + 4, search.isEmpty() ? 0x777777 : 0xE6EDF5, false);
    }

    private Optional<VanillaCreativeTabView> hoveredTab(int mouseX, int mouseY) {
        return hoveredTabIndex(mouseX, mouseY).map(index -> tabs().get(index));
    }

    private Optional<Integer> hoveredTabIndex(int mouseX, int mouseY) {
        int y = tabTop();
        if (mouseY < y || mouseY >= y + 18) {
            return Optional.empty();
        }
        int visibleIndex = (int) Math.floor((mouseX - (left + PAD) + (tabScrollPosition - Math.floor(tabScrollPosition)) * TAB) / TAB);
        int index = (int) Math.floor(tabScrollPosition) + visibleIndex;
        if (visibleIndex >= 0 && visibleIndex < visibleTabCount() + 1 && index >= 0 && index < tabs().size()) {
            return Optional.of(index);
        }
        return Optional.empty();
    }

    private Optional<Integer> hoveredSlot(int mouseX, int mouseY) {
        int gridLeft = gridLeft();
        int gridTop = gridTop();
        int columns = columns();
        int col = (mouseX - gridLeft) / SLOT;
        int localY = mouseY - gridTop;
        int row = localY / SLOT;
        if (col < 0 || row < 0 || col >= columns || row >= visibleRows()) {
            return Optional.empty();
        }
        int index = ((itemScrollPixels + localY) / SLOT) * columns + col;
        return index >= 0 && index < visibleItems().size() ? Optional.of(index) : Optional.empty();
    }

    private boolean inSearch(double mouseX, double mouseY) {
        return mouseX >= left + PAD && mouseX < left + width - PAD
                && mouseY >= searchTop() && mouseY < searchTop() + SEARCH_HEIGHT;
    }

    private boolean inTabBar(double mouseX, double mouseY) {
        return mouseX >= left + PAD && mouseX < left + width - PAD
                && mouseY >= tabTop() && mouseY < tabTop() + TAB_BAR_HEIGHT;
    }

    private boolean contains(double mouseX, double mouseY) {
        return mouseX >= left && mouseY >= top && mouseX < left + width && mouseY < top + height;
    }

    private List<VanillaCreativeTabView> tabs() {
        return GridInventoryServices.creativeTabs().tabs();
    }

    private List<CreativeItemReference> visibleItems() {
        if (!search.isBlank()) {
            String needle = search.trim().toLowerCase(Locale.ROOT);
            return allReferences().stream()
                    .filter(ref -> ref.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle))
                    .toList();
        }
        List<VanillaCreativeTabView> tabs = tabs();
        if (tabs.isEmpty()) {
            return List.of();
        }
        int tab = Math.min(selectedTab, tabs.size() - 1);
        VanillaCreativeTabView view = tabs.get(tab);
        return referencesForTab(view.sourceIndex(), view.displayItems());
    }

    private List<CreativeItemReference> allReferences() {
        ArrayList<CreativeItemReference> references = new ArrayList<>();
        List<VanillaCreativeTabView> tabs = tabs();
        for (VanillaCreativeTabView tab : tabs) {
            references.addAll(referencesForTab(tab.sourceIndex(), tab.displayItems()));
        }
        return references;
    }

    private static List<CreativeItemReference> referencesForTab(int tabIndex, List<ItemStack> stacks) {
        ArrayList<CreativeItemReference> references = new ArrayList<>();
        for (int item = 0; item < stacks.size(); item++) {
            references.add(new CreativeItemReference(tabIndex, item, stacks.get(item)));
        }
        return references;
    }

    private int columns() {
        return Math.max(1, (width - PAD * 2 - 6) / SLOT);
    }

    private int visibleRows() {
        return Math.max(1, (height - (gridTop() - top) - PAD) / SLOT);
    }

    private int clampTabScroll(int value) {
        return Math.max(0, Math.min(Math.max(0, tabs().size() - visibleTabCount()), value));
    }

    private int visibleTabCount() {
        return Math.max(1, (width - PAD * 2) / TAB);
    }

    private int tabTop() {
        return top + TITLE_HEIGHT;
    }

    private int searchTop() {
        return top + TITLE_HEIGHT + TAB_BAR_HEIGHT + SEARCH_GAP;
    }

    private int gridTop() {
        return searchTop() + SEARCH_HEIGHT + SEARCH_GAP;
    }

    private int gridLeft() {
        return left + PAD;
    }

    private static int totalRows(int itemCount, int columns) {
        return Math.max(1, (itemCount + columns - 1) / columns);
    }

    private void resetItemScroll() {
        itemScrollbar.update(gridLeft(), gridTop(), columns() * SLOT + 8, visibleRows() * SLOT, visibleRows() * SLOT);
    }

    private void scrollTabs(double deltaY) {
        if (maxTabScroll() <= 0) {
            return;
        }
        double impulse = -Math.signum(deltaY) * 0.42D;
        tabScrollVelocity = clamp(tabScrollVelocity + impulse, -1.35D, 1.35D);
    }

    private void advanceTabScroll() {
        int max = maxTabScroll();
        if (max <= 0) {
            tabScroll = 0;
            tabScrollPosition = 0.0D;
            tabScrollVelocity = 0.0D;
            return;
        }
        if (draggingTabScrollbar) {
            tabScrollVelocity = 0.0D;
        } else if (Math.abs(tabScrollVelocity) > 0.02D) {
            tabScrollPosition = clamp(tabScrollPosition + tabScrollVelocity, 0.0D, max);
            tabScrollVelocity *= 0.78D;
            tabScroll = clampTabScroll((int) Math.floor(tabScrollPosition));
        } else {
            tabScroll = clampTabScroll((int) Math.floor(tabScrollPosition));
            tabScrollVelocity = 0.0D;
        }
    }

    private void renderTabScrollbar(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int visible, int total) {
        if (total <= visible) {
            tabScrollbarAlpha = approach(tabScrollbarAlpha, 0.0F, 0.06F);
            return;
        }
        boolean active = draggingTabScrollbar || inTabBar(mouseX, mouseY);
        tabScrollbarAlpha = approach(tabScrollbarAlpha, active ? 1.0F : 0.0F, active ? 0.18F : 0.06F);
        if (tabScrollbarAlpha <= 0.03F) {
            return;
        }
        int barLeft = x;
        int barRight = x + visible * TAB - 2;
        int barY = y + 20;
        int trackAlpha = (int) (0x36 * tabScrollbarAlpha);
        int thumbAlpha = (int) ((draggingTabScrollbar ? 0xF0 : 0xB8) * tabScrollbarAlpha);
        drawPill(graphics, barLeft, barY, barRight - barLeft, 2, argb(trackAlpha, 225, 228, 232));
        int thumbWidth = tabThumbWidth(barLeft, barRight, visible, total);
        drawPill(graphics, tabThumbX(barLeft, barRight, thumbWidth), barY - 1, thumbWidth, 4, argb(thumbAlpha, 210, 218, 226));
    }

    private boolean mouseClickedTabScrollbar(double mouseX, double mouseY, int button) {
        if (button != 0 || maxTabScroll() <= 0 || !inTabScrollbar(mouseX, mouseY)) {
            return false;
        }
        int barLeft = left + PAD;
        int barRight = barLeft + visibleTabCount() * TAB - 2;
        int thumbWidth = tabThumbWidth(barLeft, barRight, visibleTabCount(), tabs().size());
        int thumbX = tabThumbX(barLeft, barRight, thumbWidth);
        if (mouseX >= thumbX && mouseX < thumbX + thumbWidth) {
            draggingTabScrollbar = true;
            tabDragOffset = (int) mouseX - thumbX;
        } else {
            moveTabThumbTo((int) mouseX - thumbWidth / 2, thumbWidth);
            draggingTabScrollbar = true;
            tabDragOffset = thumbWidth / 2;
        }
        return true;
    }

    private boolean mouseDraggedTabScrollbar(double mouseX, double mouseY, int button) {
        if (button != 0 || !draggingTabScrollbar) {
            return false;
        }
        int barLeft = left + PAD;
        int barRight = barLeft + visibleTabCount() * TAB - 2;
        int thumbWidth = tabThumbWidth(barLeft, barRight, visibleTabCount(), tabs().size());
        moveTabThumbTo((int) mouseX - tabDragOffset, thumbWidth);
        return true;
    }

    private boolean mouseReleasedTabScrollbar(int button) {
        if (button != 0 || !draggingTabScrollbar) {
            return false;
        }
        draggingTabScrollbar = false;
        return true;
    }

    private void moveTabThumbTo(int thumbX, int thumbWidth) {
        int barLeft = left + PAD;
        int barRight = barLeft + visibleTabCount() * TAB - 2;
        int travel = Math.max(1, barRight - barLeft - thumbWidth);
        int relative = clamp(thumbX - barLeft, 0, travel);
        tabScrollPosition = clamp((double) relative * maxTabScroll() / travel, 0.0D, maxTabScroll());
        tabScroll = clampTabScroll((int) Math.floor(tabScrollPosition));
        tabScrollVelocity = 0.0D;
    }

    private boolean inTabScrollbar(double mouseX, double mouseY) {
        int y = tabTop() + 18;
        return mouseX >= left + PAD && mouseX < left + PAD + visibleTabCount() * TAB
                && mouseY >= y && mouseY < y + 8;
    }

    private int tabThumbWidth(int barLeft, int barRight, int visible, int total) {
        return Math.max(12, (barRight - barLeft) * visible / total);
    }

    private int tabThumbX(int barLeft, int barRight, int thumbWidth) {
        int max = Math.max(1, maxTabScroll());
        return barLeft + (int) Math.round((barRight - barLeft - thumbWidth) * tabScrollPosition / max);
    }

    private int maxTabScroll() {
        return Math.max(0, tabs().size() - visibleTabCount());
    }

    private static void drawPill(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        if (height <= 0 || width <= 0) {
            return;
        }
        if (height <= 2) {
            graphics.fill(x + 1, y, x + width - 1, y + height, color);
            return;
        }
        graphics.fill(x + 1, y, x + width - 1, y + height, color);
        graphics.fill(x, y + 1, x + width, y + height - 1, color);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (clamp(alpha, 0, 255) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static float approach(float value, float target, float step) {
        if (value < target) {
            return Math.min(target, value + step);
        }
        return Math.max(target, value - step);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
