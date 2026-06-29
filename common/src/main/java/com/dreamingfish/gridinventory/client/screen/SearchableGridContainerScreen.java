package com.dreamingfish.gridinventory.client.screen;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.client.screen.panel.ContainerGridSidebarPanel;
import com.dreamingfish.gridinventory.client.sound.GridInventoryUiSounds;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import com.dreamingfish.gridinventory.common.network.MovePlayerSlotToSearchableContainerMessage;
import com.dreamingfish.gridinventory.common.network.MoveSearchableContainerEntryMessage;
import com.dreamingfish.gridinventory.common.network.MoveSearchableContainerEntryToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class SearchableGridContainerScreen extends GridInventoryScreen {
    private final SearchableGridContainerMenu searchableMenu;
    private GridEntry draggingContainerEntry;

    public SearchableGridContainerScreen(GridInventoryMenu menu, Inventory playerInventory, Component title) {
        this((SearchableGridContainerMenu) menu, playerInventory, title);
    }

    public SearchableGridContainerScreen(SearchableGridContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.searchableMenu = menu;
    }

    @Override
    protected void init() {
        super.init();
        rightSidebarPanel.enableContainerTab(searchableMenu.getContainerGridData(), searchableMenu.containerTitle());
    }

    @Override
    protected ItemStack draggedStack() {
        return draggingContainerEntry != null ? draggingContainerEntry.stack() : super.draggedStack();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Optional<ContainerGridSidebarPanel.GridEntryHit> containerHit =
                rightSidebarPanel.containerEntryAt((int) mouseX, (int) mouseY);
        if (button == 0 && containerHit.isPresent() && draggedStack().isEmpty()) {
            GridEntry entry = containerHit.get().entry();
            draggingContainerEntry = entry;
            dragPreviewStack = entry.stack().copy();
            rotatedPreview = entry.rotated();
            setGridDragAnchor((int) mouseX, (int) mouseY, containerHit.get().drawX(), containerHit.get().drawY(),
                    entry.width(), entry.height());
            GridInventoryUiSounds.dragStart();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingContainerEntry != null) {
            boolean released = false;
            Optional<ContainerGridSidebarPanel.GridPlacementHit> placement =
                    rightSidebarPanel.containerPlacementAt((int) mouseX, (int) mouseY);
            if (placement.isPresent()) {
                ContainerGridSidebarPanel.GridPlacementHit hit = placement.get();
                GridInventoryServices.network().sendToServer(new MoveSearchableContainerEntryMessage(
                        searchableMenu.blockPos(), draggingContainerEntry.entryId(),
                        hit.cellX() - anchorCellX(draggedStack()), hit.cellY() - anchorCellY(draggedStack()),
                        rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                released = true;
            } else {
                Slot hovered = findHoveredSlot(mouseX, mouseY);
                if (hovered != null) {
                    GridInventoryServices.network().sendToServer(new MoveSearchableContainerEntryToPlayerSlotMessage(
                            searchableMenu.blockPos(), draggingContainerEntry.entryId(), hovered.getSlotIndex(),
                            draggingContainerEntry.stack().getCount()));
                    released = true;
                }
            }
            playReleaseSound(released, false, false);
            clearDragState();
            return true;
        }
        if (button == 0 && !selectedPlayerStack.isEmpty() && lastPlayerSlot >= 0) {
            Optional<ContainerGridSidebarPanel.GridPlacementHit> placement =
                    rightSidebarPanel.containerPlacementAt((int) mouseX, (int) mouseY);
            if (placement.isPresent()) {
                ContainerGridSidebarPanel.GridPlacementHit hit = placement.get();
                GridInventoryServices.network().sendToServer(new MovePlayerSlotToSearchableContainerMessage(
                        searchableMenu.blockPos(), lastPlayerSlot,
                        hit.cellX() - anchorCellX(draggedStack()), hit.cellY() - anchorCellY(draggedStack()),
                        rotatedPreview, GridBackpackItem.isFolded(draggedStack())));
                playReleaseSound(true, false, false);
                clearDragState();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderContainerPlacementPreview(graphics, mouseX, mouseY);
    }

    private void renderContainerPlacementPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack stack = draggedStack();
        if (stack.isEmpty()) {
            return;
        }
        Optional<ContainerGridSidebarPanel.GridPlacementHit> placement = rightSidebarPanel.containerPlacementAt(mouseX, mouseY);
        if (placement.isEmpty()) {
            return;
        }
        ContainerGridSidebarPanel.GridPlacementHit hit = placement.get();
        int targetX = hit.cellX() - anchorCellX(stack);
        int targetY = hit.cellY() - anchorCellY(stack);
        GridItemSize size = GridItemSizeManager.getSize(stack);
        int w = size.placedWidth(rotatedPreview);
        int h = size.placedHeight(rotatedPreview);
        boolean valid = GridPlacementValidator.canPlace(searchableMenu.getContainerGridData(), stack, targetX, targetY,
                rotatedPreview, draggingContainerEntry == null ? null : draggingContainerEntry.entryId(), 0);
        renderPlacementPreview(graphics, searchableMenu.getContainerGridData(), hit.gridLeft(), hit.gridTop(),
                targetX, targetY, w, h, hit.cellX(), hit.cellY(),
                valid ? 0x6630C860 : 0x66D84040, valid ? 0xCC7DFFA2 : 0xCCFF8888);
    }

    @Override
    protected void clearDragState() {
        super.clearDragState();
        draggingContainerEntry = null;
    }
}
