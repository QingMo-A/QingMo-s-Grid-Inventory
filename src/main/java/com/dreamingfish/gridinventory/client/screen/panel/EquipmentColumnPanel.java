package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import com.dreamingfish.gridinventory.client.screen.widget.FreeSlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EquipmentColumnPanel {
    private int left;
    private int top;
    private int width;
    private int height;
    private final List<FreeSlotWidget> freeSlots = new ArrayList<>();

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void layoutSlots(int hotbarTop) {
        freeSlots.clear();
        int armorSize = GridInventoryClientConfig.EQUIPMENT_FREE_SLOT_SIZE.get();
        int hotbarSize = Math.min(GridInventoryClientConfig.HOTBAR_FREE_SLOT_SIZE.get(),
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
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int hotbarTop, List<Slot> slots, int draggedPlayerSlot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        layoutSlots(hotbarTop);
        graphics.fill(left, top, left + width, top + height, 0xD91A1A1A);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.equipment"), left + 8, top + 7, 0xFFFFFF, false);

        FreeSlotWidget helmet = freeSlots.get(0);
        FreeSlotWidget legs = freeSlots.get(2);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.armor_upper"), helmet.x(), helmet.y() - 12, 0x8F8F8F, false);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.armor_lower"), legs.x(), legs.y() - 12, 0x8F8F8F, false);

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
            freeSlot.render(graphics, slot, freeSlot.contains(mouseX, mouseY),
                    draggedPlayerSlot >= 0 && slot.getSlotIndex() == draggedPlayerSlot);
        }
    }

    public Optional<FreeSlotWidget> slotAt(double mouseX, double mouseY) {
        return freeSlots.stream().filter(slot -> slot.contains(mouseX, mouseY)).findFirst();
    }

    public Optional<FreeSlotWidget> slotForMenuIndex(int menuIndex) {
        return freeSlots.stream().filter(slot -> slot.menuIndex() == menuIndex).findFirst();
    }
}
