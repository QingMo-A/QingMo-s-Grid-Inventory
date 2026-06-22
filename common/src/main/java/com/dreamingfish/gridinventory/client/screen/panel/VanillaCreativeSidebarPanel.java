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
    private int scrollRows;
    private int tabScroll;
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
        scrollRows = clampScroll(scrollRows);
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
        int hoveredIndex = hoveredSlot(mouseX, mouseY).orElse(-1);
        graphics.enableScissor(gridLeft, gridTop, gridLeft + columns * SLOT, gridTop + rows * SLOT);
        for (int index = scrollRows * columns; index < items.size(); index++) {
            int local = index - scrollRows * columns;
            int row = local / columns;
            if (row >= rows) {
                break;
            }
            int col = local % columns;
            int x = gridLeft + col * SLOT;
            int y = gridTop + row * SLOT;
            ItemStack stack = items.get(index).stack();
            graphics.fill(x, y, x + SLOT, y + SLOT, hoveredIndex == index ? 0x665A6F9A : 0x55333333);
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }
        graphics.disableScissor();
        renderItemScrollbar(graphics);
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
        Optional<Integer> tab = hoveredTabIndex((int) mouseX, (int) mouseY);
        if (tab.isPresent()) {
            selectedTab = tab.get();
            tabScroll = clampTabScroll(tabScroll);
            scrollRows = 0;
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
        return button == 0 && draggedItem != null;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return button == 0 && draggedItem != null;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        if (inTabBar(mouseX, mouseY)) {
            tabScroll = clampTabScroll(tabScroll - (int) Math.signum(deltaY));
        } else {
            scrollRows = clampScroll(scrollRows - (int) Math.signum(deltaY));
        }
        return true;
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
            scrollRows = 0;
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint) {
        if (!searchFocused || Character.isISOControl(codePoint)) {
            return false;
        }
        search += codePoint;
        scrollRows = 0;
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
        graphics.enableScissor(x, y, x + visible * TAB, y + 18);
        for (int index = tabScroll; index < tabs.size(); index++) {
            int tx = x + (index - tabScroll) * TAB;
            if (index - tabScroll >= visible) {
                break;
            }
            int color = index == selectedTab ? 0xFF505A70 : 0xFF2B303A;
            graphics.fill(tx, y, tx + 18, y + 18, color);
            graphics.renderItem(tabs.get(index).icon(), tx + 1, y + 1);
        }
        graphics.disableScissor();
        if (tabs.size() > visible) {
            int barLeft = x;
            int barRight = x + visible * TAB - 2;
            int barY = y + 20;
            graphics.fill(barLeft, barY, barRight, barY + 2, 0x774B5563);
            int thumbWidth = Math.max(10, (barRight - barLeft) * visible / tabs.size());
            int maxScroll = Math.max(1, tabs.size() - visible);
            int thumbX = barLeft + (barRight - barLeft - thumbWidth) * tabScroll / maxScroll;
            graphics.fill(thumbX, barY, thumbX + thumbWidth, barY + 2, 0xFFC7D2FE);
        }
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
        int visibleIndex = (mouseX - (left + PAD)) / TAB;
        int index = tabScroll + visibleIndex;
        if (visibleIndex >= 0 && visibleIndex < visibleTabCount() && index >= 0 && index < tabs().size()) {
            return Optional.of(index);
        }
        return Optional.empty();
    }

    private Optional<Integer> hoveredSlot(int mouseX, int mouseY) {
        int gridLeft = gridLeft();
        int gridTop = gridTop();
        int columns = columns();
        int col = (mouseX - gridLeft) / SLOT;
        int row = (mouseY - gridTop) / SLOT;
        if (col < 0 || row < 0 || col >= columns || row >= visibleRows()) {
            return Optional.empty();
        }
        int index = (scrollRows + row) * columns + col;
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
        return referencesForTab(tab, tabs.get(tab).displayItems());
    }

    private List<CreativeItemReference> allReferences() {
        ArrayList<CreativeItemReference> references = new ArrayList<>();
        List<VanillaCreativeTabView> tabs = tabs();
        for (int tab = 0; tab < tabs.size(); tab++) {
            references.addAll(referencesForTab(tab, tabs.get(tab).displayItems()));
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

    private int clampScroll(int value) {
        int maxRows = Math.max(0, (visibleItems().size() + columns() - 1) / columns() - visibleRows());
        return Math.max(0, Math.min(maxRows, value));
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

    private void renderItemScrollbar(GuiGraphics graphics) {
        int columns = columns();
        int totalRows = Math.max(1, (visibleItems().size() + columns - 1) / columns);
        int rows = visibleRows();
        if (totalRows <= rows) {
            return;
        }
        int trackLeft = left + width - PAD - 3;
        int trackTop = gridTop();
        int trackHeight = rows * SLOT;
        graphics.fill(trackLeft, trackTop, trackLeft + 2, trackTop + trackHeight, 0x774B5563);
        int thumbHeight = Math.max(10, trackHeight * rows / totalRows);
        int maxScroll = Math.max(1, totalRows - rows);
        int thumbY = trackTop + (trackHeight - thumbHeight) * scrollRows / maxScroll;
        graphics.fill(trackLeft - 1, thumbY, trackLeft + 3, thumbY + thumbHeight, 0xFFC7D2FE);
    }
}
