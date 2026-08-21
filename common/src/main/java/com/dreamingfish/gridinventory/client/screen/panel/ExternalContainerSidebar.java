package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.interaction.GridDragSession;
import com.dreamingfish.gridinventory.client.interaction.GridInteractionController;
import com.dreamingfish.gridinventory.client.interaction.GridInteractionSurface;
import com.dreamingfish.gridinventory.client.render.EquipmentStorageTooltipRenderer;
import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.client.screen.widget.CuriosSlotWidget;
import com.dreamingfish.gridinventory.client.screen.widget.FreeSlotWidget;
import com.dreamingfish.gridinventory.client.ui.GridUiLayers;
import com.dreamingfish.gridinventory.client.ui.animation.HoverAnimationTracker;
import com.dreamingfish.gridinventory.common.data.ExternalPlayerGridSnapshot;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentSlotHelper;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridPlacementValidator;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionManager;
import com.dreamingfish.gridinventory.common.network.RequestExternalPlayerGridMessage;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import com.dreamingfish.gridinventory.common.util.GridItemStacks;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ExternalContainerSidebar {
    private static final int CELL = 27;
    private static final int BAR_HEIGHT = 24;
    private static final int HANDLE_WIDTH = 18;
    private static final int HANDLE_HEIGHT = 42;
    private static Dock preferredDock = Dock.RIGHT;
    private static Tab preferredTab = Tab.GRID;
    private static boolean preferredExpanded = true;

    private final GridColumnPanel gridPanel = new GridColumnPanel();
    private final EquipmentColumnPanel equipmentPanel = new EquipmentColumnPanel();
    private final HoverAnimationTracker<UUID> pocketHoverAnimations = new HoverAnimationTracker<>();
    private final GridInteractionController dragInteraction = new GridInteractionController(CELL);
    private final GridInteractionController carriedInteraction = new GridInteractionController(CELL);
    private boolean active;
    private boolean expanded;
    private Dock dock = preferredDock;
    private Tab tab = preferredTab;
    private int left;
    private int top;
    private int width;
    private int height;
    private int screenWidth;
    private int screenHeight;
    private int handleX;
    private int handleY;
    private GridInventoryData pocket = PlayerPocketDefinitionManager.createInventory();
    private List<Slot> equipmentSlots = List.of();
    private Inventory equipmentInventory;
    private UUID draggedPocketEntryId;
    private GridColumnPanel.EquipmentEntryHit draggedEquipmentEntry;
    private int draggedPlayerSlot = -1;
    private ItemStack observedCarried = ItemStack.EMPTY;
    private UUID pocketOwnerId;
    private long pocketRevision = -1L;

    public void init(Screen screen, AbstractContainerMenu menu, int screenWidth, int screenHeight,
                     int imageWidth, int imageHeight) {
        this.active = supports(screen, menu);
        if (!active) {
            clearDrag();
            carriedInteraction.clear();
            return;
        }
        carriedInteraction.clear();
        observedCarried = ItemStack.EMPTY;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.dock = preferredDock;
        this.tab = preferredTab;
        boolean hasDockSpace = screenWidth >= imageWidth + 244;
        this.expanded = preferredExpanded && (hasDockSpace || screenWidth >= 360);
        layout();
        GridInventoryServices.network().sendToServer(RequestExternalPlayerGridMessage.INSTANCE);
    }

    public boolean isDraggingSidebarItem() {
        return dragInteraction.drag().isActive() && dragInteraction.drag().source().isPresent();
    }

    public boolean contains(double mouseX, double mouseY) {
        if (!active) {
            return false;
        }
        if (!expanded) {
            return mouseX >= handleX && mouseY >= handleY
                    && mouseX < handleX + HANDLE_WIDTH && mouseY < handleY + HANDLE_HEIGHT;
        }
        return mouseX >= left && mouseY >= top && mouseX < left + width && mouseY < top + height;
    }

    public void render(GuiGraphics graphics, AbstractContainerMenu menu, @Nullable Slot hoveredMenuSlot,
                       int menuLeft, int menuTop, int mouseX, int mouseY) {
        if (!active || Minecraft.getInstance().player == null) {
            return;
        }
        refreshPocket();
        ensureEquipmentSlots();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, GridUiLayers.NESTED_WINDOW_BASE - 100.0F);
        if (!expanded) {
            renderCollapsedHandle(graphics, mouseX, mouseY);
            graphics.pose().popPose();
            return;
        }
        graphics.fill(left, top, left + width, top + height, 0xF012171C);
        renderTopBar(graphics, mouseX, mouseY);
        int contentTop = top + BAR_HEIGHT;
        int contentHeight = height - BAR_HEIGHT;
        GridDragSession movingSession = movingSession(menu);
        ItemStack moving = movingSession.stack();
        if (tab == Tab.GRID) {
            gridPanel.setBounds(left, contentTop, width, contentHeight);
            gridPanel.render(graphics, pocket, draggedPocketEntryId, draggedEquipmentEntry,
                    mouseX, mouseY, pocketHoverAnimations, !isDraggingSidebarItem() && menu.getCarried().isEmpty());
            renderGridPlacementPreview(graphics, mouseX, mouseY, movingSession);
            pocketHoverAnimations.markFrameEnd();
        } else {
            equipmentPanel.setBounds(left, contentTop, width, contentHeight);
            int hotbarTop = top + height - 28;
            equipmentPanel.render(graphics, mouseX, mouseY, mouseX, mouseY, hotbarTop,
                    equipmentSlots, draggedPlayerSlot, moving, !moving.isEmpty(), !isDraggingSidebarItem());
        }
        graphics.pose().popPose();

        if (isDraggingSidebarItem() && hoveredMenuSlot != null && !contains(mouseX, mouseY)) {
            renderMenuSlotPreview(graphics, hoveredMenuSlot, menuLeft, menuTop);
        }
        renderHoverTooltip(graphics, menu, mouseX, mouseY);
        renderDraggedOverlay(graphics, menu, mouseX, mouseY);
    }

    public boolean mouseClicked(AbstractContainerMenu menu, double mouseX, double mouseY, int button) {
        if (!active) {
            return false;
        }
        if (!expanded) {
            if (contains(mouseX, mouseY) && button == 0) {
                setExpanded(true);
                return true;
            }
            return false;
        }
        GridInteractionController movingInteraction = movingInteraction(menu);
        if (button == 1 && (isDraggingSidebarItem() || contains(mouseX, mouseY))
                && movingInteraction.handleMouseButton(button)) {
            return true;
        }
        if (button == 0 && inCollapseButton(mouseX, mouseY)) {
            setExpanded(false);
            return true;
        }
        if (button == 0 && inDockButton(mouseX, mouseY)) {
            dock = dock == Dock.LEFT ? Dock.RIGHT : Dock.LEFT;
            preferredDock = dock;
            layout();
            return true;
        }
        if (button == 0 && inEquipmentTab(mouseX, mouseY)) {
            tab = Tab.EQUIPMENT;
            preferredTab = tab;
            return true;
        }
        if (button == 0 && inGridTab(mouseX, mouseY)) {
            tab = Tab.GRID;
            preferredTab = tab;
            return true;
        }
        if (!contains(mouseX, mouseY)) {
            return false;
        }
        if (!menu.getCarried().isEmpty()) {
            return true;
        }
        if (tab == Tab.GRID) {
            if (gridPanel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (button == 0) {
                Optional<PocketHit> pocketHit = pocketHit((int) mouseX, (int) mouseY);
                if (pocketHit.isPresent() && pocketHit.get().entry() != null) {
                    beginPocketDrag(pocketHit.get());
                    return true;
                }
                Optional<GridColumnPanel.EquipmentEntryHit> equipmentHit =
                        gridPanel.equipmentEntryAt((int) mouseX, (int) mouseY);
                if (equipmentHit.isPresent()) {
                    beginEquipmentDrag(equipmentHit.get());
                    return true;
                }
            }
        } else {
            if (equipmentPanel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (button == 0) {
                Optional<FreeSlotWidget> freeSlot = equipmentPanel.slotAt(mouseX, mouseY);
                if (freeSlot.isPresent()) {
                    Slot slot = equipmentSlots.get(freeSlot.get().menuIndex());
                    if (slot.hasItem()) {
                        beginPlayerSlotDrag(slot.getSlotIndex(), slot.getItem());
                    }
                    return true;
                }
                Optional<CuriosSlotWidget> curio = equipmentPanel.curioSlotAt(mouseX, mouseY);
                if (curio.isPresent()) {
                    if (!curio.get().view().stack().isEmpty()) {
                        beginCurioDrag(curio.get());
                    }
                    return true;
                }
            }
        }
        return true;
    }

    public boolean mouseReleased(AbstractContainerMenu menu, @Nullable Slot hoveredMenuSlot,
                                 double mouseX, double mouseY, int button) {
        if (!active || button != 0) {
            return false;
        }
        if (expanded) {
            gridPanel.mouseReleased(button);
            equipmentPanel.mouseReleased(button);
        }
        if (isDraggingSidebarItem()) {
            GridInteractionController.ReleaseResult release = dragInteraction.release((int) mouseX, (int) mouseY,
                    List.of(menuSlotSurface(menu, hoveredMenuSlot), sidebarSurface()));
            clearDrag();
            return release.handled() || contains(mouseX, mouseY);
        }
        if (!menu.getCarried().isEmpty() && expanded && contains(mouseX, mouseY)) {
            synchronizeCarried(menu);
            carriedInteraction.release((int) mouseX, (int) mouseY, List.of(sidebarSurface()));
            return true;
        }
        return expanded && contains(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!active || !expanded) {
            return false;
        }
        if (button == 0 && inEquipmentTab(mouseX, mouseY)) {
            tab = Tab.EQUIPMENT;
            preferredTab = tab;
            return true;
        }
        if (button == 0 && inGridTab(mouseX, mouseY)) {
            tab = Tab.GRID;
            preferredTab = tab;
            return true;
        }
        if (gridPanel.mouseDragged(mouseX, mouseY, button)
                || equipmentPanel.mouseDragged(mouseX, mouseY, button)) {
            return true;
        }
        return isDraggingSidebarItem() && button == 0 || contains(mouseX, mouseY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (!active || !expanded || !contains(mouseX, mouseY)) {
            return false;
        }
        return tab == Tab.GRID
                ? gridPanel.mouseScrolled(mouseX, mouseY, deltaY)
                : equipmentPanel.mouseScrolled(mouseX, mouseY, deltaY);
    }

    public boolean keyPressed(AbstractContainerMenu menu, int keyCode, int scanCode) {
        if (!active) {
            return false;
        }
        return movingInteraction(menu).handlePreviewKey(keyCode, scanCode);
    }

    private static boolean supports(Screen screen, AbstractContainerMenu menu) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && !minecraft.player.isSpectator()
                && GridInventoryServices.config().enableGridInventory()
                && GridInventoryServices.config().replaceSurvivalInventory()
                && GridInventoryServices.clientConfig().enableSurvivalInventoryGridUi()
                && !(screen instanceof GridInventoryScreen)
                && !(screen instanceof InventoryScreen)
                && !(screen instanceof CreativeModeInventoryScreen)
                && menu != minecraft.player.inventoryMenu;
    }

    private void layout() {
        width = Math.min(224, Math.max(180, screenWidth - 16));
        height = screenHeight;
        top = 0;
        left = dock == Dock.LEFT ? 8 : screenWidth - width - 8;
        handleX = dock == Dock.LEFT ? 0 : screenWidth - HANDLE_WIDTH;
        handleY = Math.max(0, (screenHeight - HANDLE_HEIGHT) / 2);
    }

    private void refreshPocket() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            pocket = PlayerPocketDefinitionManager.createInventory();
            pocketOwnerId = null;
            pocketRevision = -1L;
            return;
        }
        UUID ownerId = minecraft.player.getUUID();
        if (!ownerId.equals(pocketOwnerId)) {
            pocketOwnerId = ownerId;
            pocketRevision = -1L;
        }
        Optional<ExternalPlayerGridSnapshot.Snapshot> update =
                ExternalPlayerGridSnapshot.copyIfNew(ownerId, pocketRevision);
        if (update.isPresent()) {
            pocket = update.get().data();
            pocketRevision = update.get().revision();
        } else if (pocketRevision < 0L) {
            pocket = GridInventoryServices.playerData().getPlayerGridInventory(minecraft.player).copy();
            pocketRevision = 0L;
        }
    }

    private void ensureEquipmentSlots() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || equipmentInventory == minecraft.player.getInventory()) {
            return;
        }
        equipmentInventory = minecraft.player.getInventory();
        List<Slot> slots = new ArrayList<>();
        slots.add(new SidebarPlayerSlot(equipmentInventory, 39, EquipmentSlot.HEAD));
        slots.add(new SidebarPlayerSlot(equipmentInventory, 38, EquipmentSlot.CHEST));
        slots.add(new SidebarPlayerSlot(equipmentInventory, 37, EquipmentSlot.LEGS));
        slots.add(new SidebarPlayerSlot(equipmentInventory, 36, EquipmentSlot.FEET));
        slots.add(new SidebarPlayerSlot(equipmentInventory, 40, null));
        for (int index = 0; index < 9; index++) {
            slots.add(new SidebarPlayerSlot(equipmentInventory, index, null));
        }
        equipmentSlots = List.copyOf(slots);
    }

    private void renderCollapsedHandle(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY);
        graphics.fill(handleX, handleY, handleX + HANDLE_WIDTH, handleY + HANDLE_HEIGHT,
                hovered ? 0xF02B333D : 0xE820252C);
        graphics.renderOutline(handleX, handleY, HANDLE_WIDTH, HANDLE_HEIGHT,
                hovered ? 0xFF8FB7D8 : 0xFF52606D);
        int centerX = handleX + HANDLE_WIDTH / 2;
        int centerY = handleY + HANDLE_HEIGHT / 2;
        int direction = dock == Dock.LEFT ? 1 : -1;
        graphics.fill(centerX - direction * 3, centerY - 5, centerX + direction, centerY - 3, 0xFFDCE7EF);
        graphics.fill(centerX - direction, centerY - 3, centerX + direction * 3, centerY - 1, 0xFFDCE7EF);
        graphics.fill(centerX - direction, centerY + 1, centerX + direction * 3, centerY + 3, 0xFFDCE7EF);
        graphics.fill(centerX - direction * 3, centerY + 3, centerX + direction, centerY + 5, 0xFFDCE7EF);
    }

    private void renderTopBar(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(left, top, left + width, top + BAR_HEIGHT, 0xF0181D23);
        graphics.renderOutline(left, top, width, height, 0xFF46515D);
        renderButton(graphics, collapseButtonX(), top + 3, 18, 18, inCollapseButton(mouseX, mouseY), "-");
        renderButton(graphics, dockButtonX(), top + 3, 18, 18, inDockButton(mouseX, mouseY), dock == Dock.LEFT ? ">" : "<");
        renderTab(graphics, equipmentTabX(), Component.translatable("screen.df_grid_inventory.equipment"),
                tab == Tab.EQUIPMENT, inEquipmentTab(mouseX, mouseY));
        renderTab(graphics, gridTabX(), Component.translatable("screen.df_grid_inventory.storage"),
                tab == Tab.GRID, inGridTab(mouseX, mouseY));
        if (inCollapseButton(mouseX, mouseY)) {
            renderTextTooltip(graphics, Component.translatable("tooltip.df_grid_inventory.sidebar_collapse"), mouseX, mouseY);
        } else if (inDockButton(mouseX, mouseY)) {
            renderTextTooltip(graphics, Component.translatable(dock == Dock.LEFT
                    ? "tooltip.df_grid_inventory.sidebar_move_right"
                    : "tooltip.df_grid_inventory.sidebar_move_left"), mouseX, mouseY);
        }
    }

    private void renderButton(GuiGraphics graphics, int x, int y, int buttonWidth, int buttonHeight,
                              boolean hovered, String label) {
        graphics.fill(x, y, x + buttonWidth, y + buttonHeight, hovered ? 0xFF34414D : 0xFF252D35);
        graphics.renderOutline(x, y, buttonWidth, buttonHeight, hovered ? 0xFF91BAD9 : 0xFF53606B);
        int textX = x + (buttonWidth - Minecraft.getInstance().font.width(label)) / 2;
        graphics.drawString(Minecraft.getInstance().font, label, textX, y + 5, 0xFFE7EEF3, false);
    }

    private void renderTab(GuiGraphics graphics, int x, Component label, boolean selected, boolean hovered) {
        int tabWidth = 62;
        int color = selected ? 0xFF354554 : hovered ? 0xFF29343E : 0xFF20272E;
        graphics.fill(x, top + 3, x + tabWidth, top + 21, color);
        if (selected) {
            graphics.fill(x, top + 20, x + tabWidth, top + 22, 0xFF78B4D5);
        }
        int textX = x + Math.max(4, (tabWidth - Minecraft.getInstance().font.width(label)) / 2);
        graphics.drawString(Minecraft.getInstance().font, label, textX, top + 8,
                selected ? 0xFFFFFFFF : 0xFFB6C0C8, false);
    }

    private void renderGridPlacementPreview(GuiGraphics graphics, int mouseX, int mouseY,
                                            GridDragSession session) {
        ItemStack moving = session.stack();
        if (moving.isEmpty()) {
            return;
        }
        Optional<PocketHit> pocketHit = pocketHit(mouseX, mouseY);
        if (pocketHit.isPresent()) {
            GridItemTarget.PlayerGridPlacement target = pocketTarget(pocketHit.get(), session);
            UUID ignored = session.source().filter(GridItemSource.PlayerGridEntry.class::isInstance)
                    .map(GridItemSource.PlayerGridEntry.class::cast).map(GridItemSource.PlayerGridEntry::entryId)
                    .orElse(null);
            boolean valid = GridPlacementValidator.canPlace(pocket, moving, target.x(), target.y(),
                    target.rotated(), ignored, 0);
            renderPlacement(graphics, pocket, gridPanel.pocketLeft(), gridPanel.pocketTop(), target.x(), target.y(),
                    session, valid);
            return;
        }
        gridPanel.equipmentRegionAt(mouseX, mouseY).ifPresent(region -> {
            int targetX = targetCellX(region.cellX(mouseX, mouseY), session);
            int targetY = targetCellY(region.cellY(mouseX, mouseY), session);
            UUID ignored = draggedEquipmentEntry != null
                    && draggedEquipmentEntry.slot() == region.slot()
                    && draggedEquipmentEntry.containerId().equals(region.containerId())
                    ? draggedEquipmentEntry.entry().entryId() : null;
            boolean valid = GridPlacementValidator.canPlace(region.inventory(), moving,
                    targetX, targetY, session.rotated(), ignored, 1);
            renderPlacement(graphics, region.inventory(), region.left(), region.top(), targetX, targetY,
                    session, valid);
        });
    }

    private void renderPlacement(GuiGraphics graphics, GridInventoryData inventory, int gridLeft, int gridTop,
                                 int targetX, int targetY, GridDragSession session, boolean valid) {
        var size = GridItemSizeManager.getSize(session.stack());
        int placedWidth = size.placedWidth(session.rotated());
        int placedHeight = size.placedHeight(session.rotated());
        int x = gridLeft + GridLayoutMetrics.cellLeft(inventory, targetX, targetY, CELL);
        int y = gridTop + GridLayoutMetrics.cellTop(inventory, targetX, targetY, CELL);
        int width = GridLayoutMetrics.areaWidth(inventory, targetX, targetY, placedWidth, CELL);
        int height = GridLayoutMetrics.areaHeight(inventory, targetX, targetY, placedHeight, CELL);
        graphics.fill(x, y, x + width, y + height, valid ? 0x6630C860 : 0x66D84040);
        graphics.renderOutline(x, y, width, height, valid ? 0xCC7DFFA2 : 0xCCFF8888);
    }

    private void renderMenuSlotPreview(GuiGraphics graphics, Slot slot, int menuLeft, int menuTop) {
        boolean valid = isOperationSlot(slot) && canInsertIntoMenuSlot(slot, dragInteraction.drag().stack());
        int x = menuLeft + slot.x - 1;
        int y = menuTop + slot.y - 1;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, GridUiLayers.DRAGGED_ITEM - 20.0F);
        graphics.fill(x, y, x + 18, y + 18, valid ? 0x6630C860 : 0x66D84040);
        graphics.renderOutline(x, y, 18, 18, valid ? 0xCC7DFFA2 : 0xCCFF8888);
        graphics.pose().popPose();
    }

    private void renderHoverTooltip(GuiGraphics graphics, AbstractContainerMenu menu, int mouseX, int mouseY) {
        if (!expanded || isDraggingSidebarItem() || !menu.getCarried().isEmpty()) {
            return;
        }
        ItemStack stack = ItemStack.EMPTY;
        if (tab == Tab.GRID) {
            Optional<PocketHit> hit = pocketHit(mouseX, mouseY);
            if (hit.isPresent() && hit.get().entry() != null) {
                stack = hit.get().entry().stack();
            } else {
                stack = gridPanel.equipmentEntryAt(mouseX, mouseY)
                        .map(value -> value.entry().stack()).orElse(ItemStack.EMPTY);
            }
        } else {
            Optional<FreeSlotWidget> free = equipmentPanel.slotAt(mouseX, mouseY);
            if (free.isPresent()) {
                stack = equipmentSlots.get(free.get().menuIndex()).getItem();
            } else {
                stack = equipmentPanel.curioSlotAt(mouseX, mouseY)
                        .map(value -> value.view().stack()).orElse(ItemStack.EMPTY);
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        EquipmentStorageTooltipRenderer.render(graphics, stack, mouseX, mouseY, screenWidth, screenHeight,
                GridUiLayers.NESTED_WINDOW_BASE + 200.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, GridUiLayers.VANILLA_TOOLTIP);
        graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void renderDraggedOverlay(GuiGraphics graphics, AbstractContainerMenu menu, int mouseX, int mouseY) {
        GridDragSession session = movingSession(menu);
        ItemStack moving = session.stack();
        if (moving.isEmpty() || !isDraggingSidebarItem() && !contains(mouseX, mouseY)) {
            return;
        }
        var size = GridItemSizeManager.getSize(moving);
        int placedWidth = size.placedWidth(session.rotated());
        int placedHeight = size.placedHeight(session.rotated());
        int x = mouseX - session.anchorCellX() * CELL - session.anchorPixelX();
        int y = mouseY - session.anchorCellY() * CELL - session.anchorPixelY();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, GridUiLayers.DRAGGED_ITEM);
        GridItemRenderer.renderDraggedStackOverlay(graphics, moving, x, y,
                placedWidth * CELL, placedHeight * CELL, 0.82F, session.rotated());
        graphics.pose().popPose();
    }

    private Optional<GridItemTarget> sidebarTarget(int mouseX, int mouseY, GridDragSession session) {
        if (tab == Tab.GRID) {
            Optional<PocketHit> pocketHit = pocketHit(mouseX, mouseY);
            if (pocketHit.isPresent()) {
                return Optional.of(pocketTarget(pocketHit.get(), session));
            }
            Optional<GridColumnPanel.Region> equipmentRegion = gridPanel.equipmentRegionAt(mouseX, mouseY);
            if (equipmentRegion.isPresent()) {
                GridColumnPanel.Region region = equipmentRegion.get();
                return Optional.of(new GridItemTarget.EquipmentStoragePlacement(region.slot(), region.containerId(),
                        targetCellX(region.cellX(mouseX, mouseY), session),
                        targetCellY(region.cellY(mouseX, mouseY), session), session.rotated(), session.folded()));
            }
        } else {
            Optional<FreeSlotWidget> free = equipmentPanel.slotAt(mouseX, mouseY);
            if (free.isPresent()) {
                return Optional.of(new GridItemTarget.PlayerSlot(
                        equipmentSlots.get(free.get().menuIndex()).getSlotIndex()));
            }
            Optional<CuriosSlotWidget> curio = equipmentPanel.curioSlotAt(mouseX, mouseY);
            if (curio.isPresent()) {
                return Optional.of(new GridItemTarget.AccessorySlot(
                        curio.get().view().identifier(), curio.get().view().index()));
            }
        }
        return Optional.empty();
    }

    private GridItemTarget.PlayerGridPlacement pocketTarget(PocketHit hit, GridDragSession session) {
        return new GridItemTarget.PlayerGridPlacement(targetCellX(hit.cellX(), session),
                targetCellY(hit.cellY(), session), session.rotated(), session.folded());
    }

    private int targetCellX(int hoveredCell, GridDragSession session) {
        return hoveredCell - session.anchorCellX();
    }

    private int targetCellY(int hoveredCell, GridDragSession session) {
        return hoveredCell - session.anchorCellY();
    }

    private Optional<PocketHit> pocketHit(int mouseX, int mouseY) {
        if (tab != Tab.GRID || !gridPanel.pocketBodyContains(mouseX, mouseY)) {
            return Optional.empty();
        }
        int relativeX = mouseX - gridPanel.pocketLeft();
        int relativeY = mouseY - gridPanel.pocketTop();
        int cellX = GridLayoutMetrics.cellXAt(pocket, relativeX, relativeY, CELL);
        int cellY = GridLayoutMetrics.cellYAt(pocket, relativeX, relativeY, CELL);
        if (cellX < 0 || cellY < 0 || !pocket.isEnabledCell(cellX, cellY)) {
            return Optional.empty();
        }
        GridEntry entry = pocket.getEntries().stream().filter(value -> value.contains(cellX, cellY)).findFirst().orElse(null);
        return Optional.of(new PocketHit(cellX, cellY, entry));
    }

    private void beginPocketDrag(PocketHit hit) {
        GridEntry entry = hit.entry();
        dragInteraction.begin(new GridItemSource.PlayerGridEntry(entry.entryId()), entry.stack(), entry.rotated());
        draggedPocketEntryId = entry.entryId();
        dragInteraction.drag().setCellAnchor(hit.cellX() - entry.x(), hit.cellY() - entry.y());
    }

    private void beginEquipmentDrag(GridColumnPanel.EquipmentEntryHit hit) {
        dragInteraction.begin(new GridItemSource.EquipmentStorageEntry(
                hit.slot(), hit.containerId(), hit.entry().entryId()), hit.entry().stack(), hit.entry().rotated());
        draggedEquipmentEntry = hit;
    }

    private void beginPlayerSlotDrag(int playerSlot, ItemStack stack) {
        dragInteraction.begin(new GridItemSource.PlayerSlot(playerSlot), stack, false);
        draggedPlayerSlot = playerSlot;
    }

    private void beginCurioDrag(CuriosSlotWidget slot) {
        dragInteraction.begin(new GridItemSource.AccessorySlot(
                slot.view().identifier(), slot.view().index()), slot.view().stack(), false);
    }

    private void clearDrag() {
        dragInteraction.clear();
        draggedPocketEntryId = null;
        draggedEquipmentEntry = null;
        draggedPlayerSlot = -1;
    }

    private GridInteractionController movingInteraction(AbstractContainerMenu menu) {
        if (isDraggingSidebarItem()) {
            return dragInteraction;
        }
        synchronizeCarried(menu);
        return carriedInteraction;
    }

    private GridDragSession movingSession(AbstractContainerMenu menu) {
        return movingInteraction(menu).drag();
    }

    private void synchronizeCarried(AbstractContainerMenu menu) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            observedCarried = ItemStack.EMPTY;
            carriedInteraction.clear();
            return;
        }
        if (!ItemStack.matches(observedCarried, carried)) {
            observedCarried = carried.copy();
            carriedInteraction.begin(new GridItemSource.MenuCarried(menu.containerId), carried, false);
        }
    }

    private GridInteractionSurface menuSlotSurface(AbstractContainerMenu menu, @Nullable Slot hoveredMenuSlot) {
        return (session, mouseX, mouseY) -> {
            if (hoveredMenuSlot == null || contains(mouseX, mouseY)) {
                return GridInteractionSurface.DropHit.pass();
            }
            int slotIndex = menu.slots.indexOf(hoveredMenuSlot);
            if (slotIndex < 0 || !isOperationSlot(hoveredMenuSlot)) {
                return GridInteractionSurface.DropHit.pass();
            }
            return GridInteractionSurface.DropHit.target(new GridItemTarget.MenuSlot(menu.containerId, slotIndex));
        };
    }

    private GridInteractionSurface sidebarSurface() {
        return (session, mouseX, mouseY) -> {
            if (!expanded || !contains(mouseX, mouseY)) {
                return GridInteractionSurface.DropHit.pass();
            }
            return sidebarTarget(mouseX, mouseY, session)
                    .map(GridInteractionSurface.DropHit::target)
                    .orElseGet(GridInteractionSurface.DropHit::blocked);
        };
    }

    private boolean isOperationSlot(Slot slot) {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && slot.container != minecraft.player.getInventory();
    }

    private boolean canInsertIntoMenuSlot(Slot slot, ItemStack stack) {
        ItemStack moved = stack;
        if (!isOperationSlot(slot) || moved.isEmpty() || !slot.mayPlace(moved)) {
            return false;
        }
        ItemStack existing = slot.getItem();
        int limit = Math.min(slot.getMaxStackSize(), moved.getMaxStackSize());
        return existing.isEmpty() && limit > 0
                || GridItemStacks.sameItemSameData(existing, moved) && existing.getCount() < limit;
    }

    private void setExpanded(boolean expanded) {
        this.expanded = expanded;
        preferredExpanded = expanded;
        if (!expanded) {
            clearDrag();
        }
    }

    private int collapseButtonX() {
        return dock == Dock.LEFT ? left + width - 21 : left + 3;
    }

    private int dockButtonX() {
        return dock == Dock.LEFT ? left + width - 42 : left + 24;
    }

    private int equipmentTabX() {
        return left + (width - 128) / 2;
    }

    private int gridTabX() {
        return equipmentTabX() + 66;
    }

    private boolean inCollapseButton(double mouseX, double mouseY) {
        return inRect(mouseX, mouseY, collapseButtonX(), top + 3, 18, 18);
    }

    private boolean inDockButton(double mouseX, double mouseY) {
        return inRect(mouseX, mouseY, dockButtonX(), top + 3, 18, 18);
    }

    private boolean inEquipmentTab(double mouseX, double mouseY) {
        return inRect(mouseX, mouseY, equipmentTabX(), top + 3, 62, 18);
    }

    private boolean inGridTab(double mouseX, double mouseY) {
        return inRect(mouseX, mouseY, gridTabX(), top + 3, 62, 18);
    }

    private static boolean inRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private static void renderTextTooltip(GuiGraphics graphics, Component text, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, GridUiLayers.VANILLA_TOOLTIP);
        graphics.renderTooltip(Minecraft.getInstance().font, List.of(text), Optional.empty(), mouseX, mouseY);
        graphics.pose().popPose();
    }

    private enum Dock {
        LEFT,
        RIGHT
    }

    private enum Tab {
        EQUIPMENT,
        GRID
    }

    private record PocketHit(int cellX, int cellY, @Nullable GridEntry entry) {
    }

    private static final class SidebarPlayerSlot extends Slot {
        private final Inventory inventory;
        private final EquipmentSlot equipmentSlot;

        private SidebarPlayerSlot(Inventory inventory, int slot, @Nullable EquipmentSlot equipmentSlot) {
            super(inventory, slot, 0, 0);
            this.inventory = inventory;
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return equipmentSlot == null || EquipmentSlotHelper.canEquip(stack, equipmentSlot, inventory.player);
        }

        @Override
        public int getMaxStackSize() {
            return equipmentSlot == null ? super.getMaxStackSize() : 1;
        }
    }
}
