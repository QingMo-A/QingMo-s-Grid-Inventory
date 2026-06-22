package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.screen.widget.NearbyGroundItemView;
import com.dreamingfish.gridinventory.client.screen.widget.NearbyItemsPanel;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class RightSidebarPanel {
    public static final int MIN_WIDTH = NearbyItemsPanel.CELL * 4 + NearbyItemsPanel.CHROME_WIDTH;
    private final NearbyItemsPanel nearbyItemsPanel = new NearbyItemsPanel();
    private final VanillaCreativeSidebarPanel creativePanel = new VanillaCreativeSidebarPanel();
    private Page page = Page.NEARBY;
    private int left;
    private int top;
    private int width;
    private int height;
    private boolean creative;

    public void setBounds(int left, int top, int height, int columns, boolean creative) {
        this.left = left;
        this.top = top;
        this.width = columns * NearbyItemsPanel.CELL + NearbyItemsPanel.CHROME_WIDTH;
        this.height = height;
        this.creative = creative;
        nearbyItemsPanel.setBounds(left, top + 20, Math.max(NearbyItemsPanel.CELL + 26, height - 20), columns);
        creativePanel.setBounds(left, top + 20, width, Math.max(40, height - 20));
        if (creative) {
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

    public void render(GuiGraphics graphics, int mouseX, int mouseY, boolean suppressTooltip) {
        renderTabs(graphics, mouseX, mouseY);
        if (page == Page.CREATIVE && creative) {
            creativePanel.render(graphics, mouseX, mouseY, suppressTooltip);
        } else {
            nearbyItemsPanel.render(graphics, mouseX, mouseY, suppressTooltip);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= top && mouseY < top + 18 && mouseX >= left && mouseX < left + width) {
            if (!creative) {
                page = Page.NEARBY;
                return true;
            }
            int tab = (int) ((mouseX - left) / Math.max(1, width / 2));
            if (tab == 0) {
                page = Page.NEARBY;
                return true;
            }
            if (tab == 1 && creative) {
                page = Page.CREATIVE;
                return true;
            }
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
        if (!creative) {
            graphics.fill(left, top, left + width, top + 18, 0xAA44516A);
            graphics.drawString(font, Component.translatable("screen.df_grid_inventory.nearby_items"), left + 5, top + 5, 0xE6EDF5, false);
            return;
        }
        int half = Math.max(1, width / 2);
        graphics.fill(left, top, left + half, top + 18, page == Page.NEARBY ? 0xAA44516A : 0xAA252A34);
        graphics.fill(left + half, top, left + width, top + 18, page == Page.CREATIVE ? 0xAA44516A : 0xAA252A34);
        graphics.drawString(font, Component.translatable("screen.df_grid_inventory.nearby_items"), left + 5, top + 5, 0xE6EDF5, false);
        graphics.drawString(font, Component.translatable("container.creative"), left + half + 5, top + 5, 0xE6EDF5, false);
    }

    private enum Page {
        NEARBY,
        CREATIVE
    }
}
