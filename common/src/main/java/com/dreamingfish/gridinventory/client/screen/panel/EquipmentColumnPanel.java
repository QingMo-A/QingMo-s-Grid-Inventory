package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.client.screen.widget.CuriosSlotWidget;
import com.dreamingfish.gridinventory.client.screen.widget.FreeSlotWidget;
import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EquipmentColumnPanel {
    private int left;
    private int top;
    private int width;
    private int height;
    private int curiosToggleX;
    private int curiosToggleY;
    private int curiosToggleSize = 14;
    private boolean curiosExpanded;
    private int curiosScroll;
    private int curiosTotalSlots;
    private int curiosViewportX;
    private int curiosViewportY;
    private int curiosViewportWidth;
    private int curiosViewportHeight;
    private static final int MAX_VISIBLE_CURIOS_SLOTS = 5;
    private final List<FreeSlotWidget> freeSlots = new ArrayList<>();
    private final List<CuriosSlotWidget> curiosSlots = new ArrayList<>();

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void layoutSlots(int hotbarTop) {
        freeSlots.clear();
        int armorSize = GridInventoryServices.clientConfig().equipmentFreeSlotSize();
        int hotbarSize = Math.min(GridInventoryServices.clientConfig().hotbarFreeSlotSize(),
                Math.max(18, (width - 24) / 10));
        int armorGap = Math.max(18, Math.min(34, (width - armorSize * 2) / 3));
        int pairLeft = left + (width - armorSize * 2 - armorGap) / 2;
        int upperY = top + 27;
        int lowerY = hotbarTop - armorSize - 16;
        freeSlots.add(new FreeSlotWidget(0, pairLeft, upperY, armorSize));
        freeSlots.add(new FreeSlotWidget(1, pairLeft + armorSize + armorGap, upperY, armorSize));
        freeSlots.add(new FreeSlotWidget(2, pairLeft, lowerY, armorSize));
        freeSlots.add(new FreeSlotWidget(3, pairLeft + armorSize + armorGap, lowerY, armorSize));
        int controlsLeft = left + 8;
        freeSlots.add(new FreeSlotWidget(4, controlsLeft, hotbarTop, hotbarSize));
        int hotbarLeft = controlsLeft + hotbarSize + 8;
        for (int index = 0; index < 9; index++) {
            freeSlots.add(new FreeSlotWidget(5 + index, hotbarLeft + index * hotbarSize, hotbarTop, hotbarSize));
        }
        layoutCuriosSlots();
    }

    private void layoutCuriosSlots() {
        curiosSlots.clear();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !CuriosIntegration.isLoaded()) {
            return;
        }
        curiosToggleX = left + width - curiosToggleSize - 8;
        curiosToggleY = top + 6;
        if (!curiosExpanded) {
            return;
        }
        int slotSize = GridInventoryServices.clientConfig().gridCellSize();
        int startX = left + 8;
        int startY = top + 45;
        List<com.dreamingfish.gridinventory.common.compat.curios.CuriosSlotView> views = CuriosIntegration.collectSlots(minecraft.player);
        curiosTotalSlots = views.size();
        int visibleSlots = Math.min(MAX_VISIBLE_CURIOS_SLOTS, curiosTotalSlots);
        curiosScroll = Math.max(0, Math.min(curiosScroll, Math.max(0, curiosTotalSlots - visibleSlots)));
        curiosViewportX = startX;
        curiosViewportY = startY;
        curiosViewportWidth = slotSize + 10;
        curiosViewportHeight = visibleSlots == 0 ? 0 : visibleSlots * slotSize + Math.max(0, visibleSlots - 1) * 4;
        for (int i = 0; i < visibleSlots; i++) {
            int viewIndex = curiosScroll + i;
            curiosSlots.add(new CuriosSlotWidget(views.get(viewIndex), startX, startY + i * (slotSize + 4), slotSize));
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int hotbarTop, List<Slot> slots,
                       int draggedPlayerSlot, ItemStack draggedStack, boolean supportsDropToFreeSlot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        layoutSlots(hotbarTop);
        graphics.fill(left, top, left + width, top + height, 0x2E1A1A1A);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.equipment"), left + 8, top + 7, 0xFFFFFF, false);
        renderCuriosToggle(graphics, mouseX, mouseY);

        FreeSlotWidget helmet = freeSlots.get(0);
        FreeSlotWidget legs = freeSlots.get(2);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.armor_upper"), helmet.x(), helmet.y() - 12, 0x8F8F8F, false);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.armor_lower"), legs.x(), legs.y() - 12, 0x8F8F8F, false);
        if (curiosExpanded && !curiosSlots.isEmpty()) {
            graphics.drawString(minecraft.font, Component.literal("Curios"), left + 8, top + 32, 0xBFA8FF, false);
        }

        int modelLeft = left + 7;
        int modelRight = left + width - 7;
        int modelTop = helmet.y() + helmet.size() + 3;
        int modelBottom = legs.y() - 3;
        int modelHeight = Math.max(1, modelBottom - modelTop);
        int scale = Math.min(52, Math.max(30, modelHeight / 2 + 8));
        graphics.enableScissor(modelLeft, modelTop, modelRight, modelBottom);
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, modelLeft, modelTop, modelRight, modelBottom, scale,
                0.0625F, mouseX, mouseY, minecraft.player
        );
        graphics.disableScissor();

        FreeSlotWidget offhand = freeSlots.get(4);
        FreeSlotWidget firstHotbar = freeSlots.get(5);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.offhand"), offhand.x(), hotbarTop - 14, 0xBFBFBF, false);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.hotbar"), firstHotbar.x(), hotbarTop - 14, 0xBFBFBF, false);
        for (FreeSlotWidget freeSlot : freeSlots) {
            Slot slot = slots.get(freeSlot.menuIndex());
            boolean hovered = freeSlot.contains(mouseX, mouseY);
            Boolean dropAllowed = !draggedStack.isEmpty()
                    ? supportsDropToFreeSlot && canReceive(slot, draggedStack, slots, draggedPlayerSlot, minecraft.player) : null;
            freeSlot.render(graphics, slot, hovered,
                    draggedPlayerSlot >= 0 && slot.getSlotIndex() == draggedPlayerSlot, dropAllowed);
        }
        for (CuriosSlotWidget curioSlot : curiosSlots) {
            boolean hovered = curioSlot.contains(mouseX, mouseY);
            Boolean dropAllowed = !draggedStack.isEmpty()
                    ? CuriosIntegration.canPlaceInCurio(minecraft.player, curioSlot.view().identifier(), curioSlot.view().index(),
                    draggedStack, curioSlot.view().stack().isEmpty()) : null;
            curioSlot.render(graphics, hovered, dropAllowed, false);
        }
        renderCuriosScrollbar(graphics);
    }

    private boolean canReceive(Slot slot, ItemStack draggedStack, List<Slot> slots, int draggedPlayerSlot, net.minecraft.world.entity.player.Player player) {
        if (!slot.mayPlace(draggedStack)) {
            return false;
        }
        ItemStack existing = slot.getItem();
        if (draggedPlayerSlot < 0) {
            return existing.isEmpty()
                    || ItemStack.isSameItemSameComponents(existing, draggedStack)
                    && existing.getCount() < Math.min(existing.getMaxStackSize(), slot.getMaxStackSize());
        }
        if (slot.getSlotIndex() == draggedPlayerSlot) {
            return false;
        }
        Optional<Slot> source = slots.stream().filter(candidate -> candidate.getSlotIndex() == draggedPlayerSlot).findFirst();
        if (source.isEmpty() || !source.get().mayPickup(player)) {
            return false;
        }
        int targetLimit = Math.min(draggedStack.getMaxStackSize(), slot.getMaxStackSize());
        if (existing.isEmpty()) {
            return targetLimit > 0;
        }
        if (ItemStack.isSameItemSameComponents(existing, draggedStack)) {
            return existing.getCount() < targetLimit;
        }
        int sourceLimit = Math.min(existing.getMaxStackSize(), source.get().getMaxStackSize());
        return slot.mayPickup(player) && source.get().mayPlace(existing)
                && draggedStack.getCount() <= targetLimit && existing.getCount() <= sourceLimit;
    }

    public Optional<FreeSlotWidget> slotAt(double mouseX, double mouseY) {
        return freeSlots.stream().filter(slot -> slot.contains(mouseX, mouseY)).findFirst();
    }

    public Optional<FreeSlotWidget> slotForMenuIndex(int menuIndex) {
        return freeSlots.stream().filter(slot -> slot.menuIndex() == menuIndex).findFirst();
    }

    public Optional<CuriosSlotWidget> curioSlotAt(double mouseX, double mouseY) {
        if (!curiosExpanded) {
            return Optional.empty();
        }
        return curiosSlots.stream().filter(slot -> slot.contains(mouseX, mouseY)).findFirst();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!curiosExpanded || curiosTotalSlots <= MAX_VISIBLE_CURIOS_SLOTS || !inCuriosViewport(mouseX, mouseY)) {
            return false;
        }
        int maxScroll = Math.max(0, curiosTotalSlots - MAX_VISIBLE_CURIOS_SLOTS);
        int next = curiosScroll + (scrollY < 0.0D ? 1 : -1);
        curiosScroll = Math.max(0, Math.min(next, maxScroll));
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !CuriosIntegration.isLoaded()) {
            return false;
        }
        if (mouseX >= curiosToggleX && mouseY >= curiosToggleY
                && mouseX < curiosToggleX + curiosToggleSize && mouseY < curiosToggleY + curiosToggleSize) {
            curiosExpanded = !curiosExpanded;
            return true;
        }
        return false;
    }

    private void renderCuriosToggle(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!CuriosIntegration.isLoaded()) {
            return;
        }
        boolean hovered = mouseX >= curiosToggleX && mouseY >= curiosToggleY
                && mouseX < curiosToggleX + curiosToggleSize && mouseY < curiosToggleY + curiosToggleSize;
        graphics.fill(curiosToggleX, curiosToggleY, curiosToggleX + curiosToggleSize, curiosToggleY + curiosToggleSize,
                hovered ? 0x88413B56 : 0x55302A42);
        graphics.renderOutline(curiosToggleX, curiosToggleY, curiosToggleSize, curiosToggleSize,
                curiosExpanded ? 0xFFBFA8FF : hovered ? 0xFFE3E8EE : 0xAA7A69A8);
        graphics.drawCenteredString(Minecraft.getInstance().font, curiosExpanded ? "-" : "+",
                curiosToggleX + curiosToggleSize / 2, curiosToggleY + 3, 0xFFEBDDFF);
    }

    private boolean inCuriosViewport(double mouseX, double mouseY) {
        return mouseX >= curiosViewportX && mouseY >= curiosViewportY
                && mouseX < curiosViewportX + curiosViewportWidth
                && mouseY < curiosViewportY + curiosViewportHeight;
    }

    private void renderCuriosScrollbar(GuiGraphics graphics) {
        if (!curiosExpanded || curiosTotalSlots <= MAX_VISIBLE_CURIOS_SLOTS || curiosViewportHeight <= 0) {
            return;
        }
        int barX = curiosViewportX + curiosViewportWidth - 4;
        int barY = curiosViewportY;
        int barHeight = curiosViewportHeight;
        graphics.fill(barX, barY, barX + 2, barY + barHeight, 0x44202020);
        int thumbHeight = Math.max(10, barHeight * MAX_VISIBLE_CURIOS_SLOTS / curiosTotalSlots);
        int maxScroll = Math.max(1, curiosTotalSlots - MAX_VISIBLE_CURIOS_SLOTS);
        int travel = Math.max(0, barHeight - thumbHeight);
        int thumbY = barY + travel * curiosScroll / maxScroll;
        graphics.fill(barX - 1, thumbY, barX + 3, thumbY + thumbHeight, 0xCCBFA8FF);
    }
}
