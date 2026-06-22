package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.creative.VanillaCreativeTabView;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class VanillaCreativeSidebarPanel {
    private static final int SLOT = 18;
    private static final int TAB = 20;
    private int left;
    private int top;
    private int width;
    private int height;
    private int scrollRows;
    private int selectedTab;
    private String search = "";
    private boolean searchFocused;
    private ItemStack draggedStack = ItemStack.EMPTY;

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        selectedTab = Math.min(selectedTab, Math.max(0, tabs().size() - 1));
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
        graphics.drawString(font, Component.translatable("screen.df_grid_inventory.creative_items"), left + 6, top + 4, 0xFFFFFF, false);
        renderTabs(graphics, mouseX, mouseY);
        renderSearch(graphics, font);
        int gridLeft = left + 6;
        int gridTop = top + 46;
        int columns = columns();
        int rows = visibleRows();
        List<ItemStack> items = visibleItems();
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
            ItemStack stack = items.get(index);
            graphics.fill(x, y, x + SLOT, y + SLOT, hoveredIndex == index ? 0x665A6F9A : 0x55333333);
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }
        graphics.disableScissor();
        if (!suppressTooltip) {
            hoveredTab(mouseX, mouseY).ifPresent(tab -> graphics.renderTooltip(font, tab.title(), mouseX, mouseY));
            hoveredSlot(mouseX, mouseY).ifPresent(index -> graphics.renderTooltip(font, items.get(index), mouseX, mouseY));
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
            draggedStack = visibleItems().get(slot.get()).copy();
            return true;
        }
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return button == 0 && !draggedStack.isEmpty();
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return button == 0 && !draggedStack.isEmpty();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        scrollRows = clampScroll(scrollRows - (int) Math.signum(deltaY));
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
        return draggedStack.isEmpty() ? Optional.empty() : Optional.of(draggedStack.copy());
    }

    public void clearDrag() {
        draggedStack = ItemStack.EMPTY;
    }

    public boolean isSearchFocused() {
        return searchFocused;
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        List<VanillaCreativeTabView> tabs = tabs();
        int x = left + 5;
        int y = top + 17;
        for (int index = 0; index < tabs.size(); index++) {
            int tx = x + index * TAB;
            if (tx + TAB > left + width - 4) {
                break;
            }
            int color = index == selectedTab ? 0xFF505A70 : 0xFF2B303A;
            graphics.fill(tx, y, tx + 18, y + 18, color);
            graphics.renderItem(tabs.get(index).icon(), tx + 1, y + 1);
        }
    }

    private void renderSearch(GuiGraphics graphics, Font font) {
        int color = searchFocused ? 0xFF8FB3FF : 0xFF606A7A;
        graphics.fill(left + 6, top + 39, left + width - 6, top + 40, color);
        Component text = search.isEmpty()
                ? Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(search);
        graphics.drawString(font, text, left + 8, top + 28, search.isEmpty() ? 0x777777 : 0xE6EDF5, false);
    }

    private Optional<VanillaCreativeTabView> hoveredTab(int mouseX, int mouseY) {
        return hoveredTabIndex(mouseX, mouseY).map(index -> tabs().get(index));
    }

    private Optional<Integer> hoveredTabIndex(int mouseX, int mouseY) {
        int y = top + 17;
        if (mouseY < y || mouseY >= y + 18) {
            return Optional.empty();
        }
        int index = (mouseX - (left + 5)) / TAB;
        if (index >= 0 && index < tabs().size() && left + 5 + index * TAB + 18 <= left + width - 4) {
            return Optional.of(index);
        }
        return Optional.empty();
    }

    private Optional<Integer> hoveredSlot(int mouseX, int mouseY) {
        int gridLeft = left + 6;
        int gridTop = top + 46;
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
        return mouseX >= left + 6 && mouseX < left + width - 6 && mouseY >= top + 26 && mouseY < top + 42;
    }

    private boolean contains(double mouseX, double mouseY) {
        return mouseX >= left && mouseY >= top && mouseX < left + width && mouseY < top + height;
    }

    private List<VanillaCreativeTabView> tabs() {
        return GridInventoryServices.creativeTabs().tabs();
    }

    private List<ItemStack> visibleItems() {
        if (!search.isBlank()) {
            return GridInventoryServices.creativeTabs().search(search);
        }
        List<VanillaCreativeTabView> tabs = tabs();
        return tabs.isEmpty() ? List.of() : tabs.get(Math.min(selectedTab, tabs.size() - 1)).displayItems();
    }

    private int columns() {
        return Math.max(1, (width - 12) / SLOT);
    }

    private int visibleRows() {
        return Math.max(1, (height - 48) / SLOT);
    }

    private int clampScroll(int value) {
        int maxRows = Math.max(0, (visibleItems().size() + columns() - 1) / columns() - visibleRows());
        return Math.max(0, Math.min(maxRows, value));
    }
}
