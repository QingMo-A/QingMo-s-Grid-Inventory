package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.screen.widget.NearbyGroundItemView;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class RightSidebarPanel {
    public static final int MIN_WIDTH = NearbyItemsPanel.CELL * 4 + NearbyItemsPanel.CHROME_WIDTH;
    private final ContainerGridSidebarPanel containerPanel = new ContainerGridSidebarPanel();
    private final NearbyItemsPanel nearbyItemsPanel = new NearbyItemsPanel();
    private final VanillaCreativeSidebarPanel creativePanel = new VanillaCreativeSidebarPanel();
    private Page page = Page.NEARBY;
    private int left;
    private int top;
    private int width;
    private int height;
    private boolean creative;
    private boolean containerTabEnabled;
    private Component containerTitle = Component.translatable("container.df_grid_inventory.searchable_grid_container");

    public void setBounds(int left, int top, int height, int columns, boolean creative) {
        this.left = left;
        this.top = top;
        this.width = columns * NearbyItemsPanel.CELL + NearbyItemsPanel.CHROME_WIDTH;
        this.height = height;
        this.creative = creative;
        nearbyItemsPanel.setBounds(left, top + 20, Math.max(NearbyItemsPanel.CELL + 26, height - 20), columns);
        containerPanel.setBounds(left, top + 20, width, Math.max(40, height - 20));
        creativePanel.setBounds(left, top + 20, width, Math.max(40, height - 20));
        if (containerTabEnabled) {
            page = Page.CONTAINER;
            if (creative) {
                GridInventoryServices.creativeTabs().refresh();
                creativePanel.selectDefaultTab();
            } else {
                creativePanel.clearDrag();
            }
        } else if (creative) {
            page = Page.CREATIVE;
            GridInventoryServices.creativeTabs().refresh();
            creativePanel.selectDefaultTab();
        } else {
            page = Page.NEARBY;
            creativePanel.clearDrag();
        }
    }

    public void setBounds(int left, int top, boolean creative) {
        int columns = GridInventoryServices.clientConfig().nearbyPanelColumns();
        setBounds(left, top, GridInventoryServices.clientConfig().nearbyPanelVisibleRows() * NearbyItemsPanel.CELL + 46,
                columns, creative);
    }

    public int width() {
        return width;
    }

    public void enableContainerTab(GridInventoryData gridData, Component title) {
        containerTabEnabled = true;
        updateContainerTab(gridData, title);
        page = Page.CONTAINER;
    }

    public void updateContainerTab(GridInventoryData gridData, Component title) {
        containerTitle = title;
        containerPanel.setContainer(gridData, title);
    }

    public boolean isContainerPage() {
        return containerTabEnabled && page == Page.CONTAINER;
    }

    public Optional<ContainerGridSidebarPanel.GridEntryHit> containerEntryAt(int mouseX, int mouseY) {
        return isContainerPage() ? containerPanel.entryAt(mouseX, mouseY) : Optional.empty();
    }

    public Optional<ContainerGridSidebarPanel.GridPlacementHit> containerPlacementAt(int mouseX, int mouseY) {
        return isContainerPage() ? containerPanel.placementAt(mouseX, mouseY) : Optional.empty();
    }

    public boolean isContainerPagePoint(double mouseX, double mouseY) {
        return isContainerPage() && containerPanel.isContainerPagePoint(mouseX, mouseY);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, boolean suppressTooltip) {
        renderTabs(graphics, mouseX, mouseY);
        if (page == Page.CONTAINER && containerTabEnabled) {
            containerPanel.render(graphics, mouseX, mouseY, suppressTooltip);
        } else if (page == Page.CREATIVE && creative) {
            creativePanel.render(graphics, mouseX, mouseY, suppressTooltip);
        } else {
            nearbyItemsPanel.render(graphics, mouseX, mouseY, suppressTooltip);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= top && mouseY < top + 18 && mouseX >= left && mouseX < left + width) {
            int tabCount = tabCount();
            int tab = Math.min(tabCount - 1, (int) ((mouseX - left) / Math.max(1, width / tabCount)));
            if (containerTabEnabled && tab == 0) {
                page = Page.CONTAINER;
                return true;
            }
            int nearbyTab = containerTabEnabled ? 1 : 0;
            int creativeTab = containerTabEnabled ? 2 : 1;
            if (tab == nearbyTab) {
                page = Page.NEARBY;
                return true;
            }
            if (tab == creativeTab && creative) {
                page = Page.CREATIVE;
                return true;
            }
        }
        if (page == Page.CONTAINER && containerTabEnabled) {
            return containerPanel.isContainerPagePoint(mouseX, mouseY);
        }
        return page == Page.CREATIVE && creative
                ? creativePanel.mouseClicked(mouseX, mouseY, button)
                : nearbyItemsPanel.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return page == Page.CREATIVE && creative
                ? creativePanel.mouseDragged(mouseX, mouseY, button)
                : nearbyItemsPanel.mouseDragged(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return page == Page.CREATIVE && creative
                ? creativePanel.mouseReleased(mouseX, mouseY, button)
                : nearbyItemsPanel.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        return page == Page.CREATIVE && creative
                ? creativePanel.mouseScrolled(mouseX, mouseY, deltaY)
                : nearbyItemsPanel.mouseScrolled(mouseX, mouseY, deltaY);
    }

    public boolean keyPressed(int keyCode) {
        return page == Page.CREATIVE && creative && creativePanel.keyPressed(keyCode);
    }

    public boolean charTyped(char codePoint) {
        return page == Page.CREATIVE && creative && creativePanel.charTyped(codePoint);
    }

    public boolean isTextInputFocused() {
        return page == Page.CREATIVE && creative && creativePanel.isSearchFocused();
    }

    public SidebarDragKind dragKind() {
        if (creative && creativePanel.draggedStack().isPresent()) {
            return SidebarDragKind.CREATIVE_ITEM;
        }
        return nearbyItemsPanel.isDraggingGroundItem() ? SidebarDragKind.NEARBY_GROUND_ITEM : SidebarDragKind.NONE;
    }

    public Optional<ItemStack> draggedStack() {
        return (creative ? creativePanel.draggedStack() : Optional.<ItemStack>empty())
                .or(() -> nearbyItemsPanel.draggedView().map(NearbyGroundItemView::stack));
    }

    public Optional<CreativeItemReference> draggedCreativeItem() {
        return creative ? creativePanel.draggedItem() : Optional.empty();
    }

    public Optional<NearbyGroundItemView> draggedGroundItem() {
        return nearbyItemsPanel.draggedView();
    }

    public boolean isDraggingGroundItem() {
        return nearbyItemsPanel.isDraggingGroundItem();
    }

    public boolean pickupHovered(int mouseX, int mouseY) {
        return nearbyItemsPanel.pickupHovered(mouseX, mouseY);
    }

    public void clearDrag() {
        nearbyItemsPanel.clearDrag();
        creativePanel.clearDrag();
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        if (!containerTabEnabled && !creative) {
            graphics.fill(left, top, left + width, top + 18, 0xAA44516A);
            graphics.drawString(font, Component.translatable("screen.df_grid_inventory.nearby_items"), left + 5, top + 5, 0xE6EDF5, false);
            return;
        }
        int tabCount = tabCount();
        int tabWidth = Math.max(1, width / tabCount);
        for (int index = 0; index < tabCount; index++) {
            Page tabPage = pageAt(index);
            int tabLeft = left + index * tabWidth;
            int tabRight = index == tabCount - 1 ? left + width : tabLeft + tabWidth;
            graphics.fill(tabLeft, top, tabRight, top + 18, page == tabPage ? 0xAA44516A : 0xAA252A34);
            graphics.drawString(font, titleFor(tabPage), tabLeft + 5, top + 5, 0xE6EDF5, false);
        }
    }

    private int tabCount() {
        return (containerTabEnabled ? 1 : 0) + 1 + (creative ? 1 : 0);
    }

    private Page pageAt(int index) {
        if (containerTabEnabled) {
            if (index == 0) {
                return Page.CONTAINER;
            }
            index--;
        }
        if (index == 0) {
            return Page.NEARBY;
        }
        return Page.CREATIVE;
    }

    private Component titleFor(Page tabPage) {
        return switch (tabPage) {
            case CONTAINER -> containerTitle;
            case NEARBY -> Component.translatable("screen.df_grid_inventory.nearby_items");
            case CREATIVE -> Component.translatable("container.creative");
        };
    }

    private enum Page {
        CONTAINER,
        NEARBY,
        CREATIVE
    }
}
