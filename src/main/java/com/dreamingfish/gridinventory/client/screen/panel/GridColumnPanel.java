package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridRenderer;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GridColumnPanel {
    private static final int CELL = 18;
    private int left;
    private int top;
    private int width;
    private int height;
    private int scroll;
    private int contentHeight;
    private final List<Region> equipmentRegions = new ArrayList<>();

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public int pocketLeft() {
        return left + 8;
    }

    public int pocketTop() {
        return top + 35 - scroll;
    }

    public void render(GuiGraphics graphics, GridInventoryData pocket, @Nullable UUID draggedPocketEntryId,
                       @Nullable EquipmentEntryHit draggedEquipmentEntry) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(left, top, left + width, top + height, 0xD91A1A1A);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.storage"), left + 8, top + 6, 0xFFFFFF, false);
        graphics.enableScissor(left, top + 18, left + width - 6, top + height - 4);
        equipmentRegions.clear();
        int y = top + 22 - scroll;
        y = renderGrid(graphics, Component.translatable("screen.df_grid_inventory.pocket"), pocket, y, draggedPocketEntryId);
        if (minecraft.player != null) {
            y = renderEquipmentStorage(graphics, EquipmentSlot.CHEST, minecraft.player.getItemBySlot(EquipmentSlot.CHEST), Component.translatable("screen.df_grid_inventory.chest_storage"), y, draggedEquipmentEntry);
            y = renderEquipmentStorage(graphics, EquipmentSlot.LEGS, minecraft.player.getItemBySlot(EquipmentSlot.LEGS), Component.translatable("screen.df_grid_inventory.legs_storage"), y, draggedEquipmentEntry);
        }
        graphics.disableScissor();
        contentHeight = Math.max(height, y - top + scroll + 6);
        renderScrollbar(graphics);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (mouseX < left || mouseX >= left + width || mouseY < top || mouseY >= top + height || contentHeight <= height) {
            return false;
        }
        int max = Math.max(0, contentHeight - height);
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(deltaY) * CELL));
        return true;
    }

    public Optional<EquipmentEntryHit> equipmentEntryAt(int mouseX, int mouseY) {
        return equipmentRegions.stream()
                .filter(region -> region.contains(mouseX, mouseY))
                .flatMap(region -> region.inventory().getEntries().stream()
                        .filter(entry -> entry.contains((mouseX - region.left()) / CELL, (mouseY - region.top()) / CELL))
                        .map(entry -> new EquipmentEntryHit(region.slot(), region.containerId(), region.inventory(), entry, region)))
                .findFirst();
    }

    public Optional<Region> equipmentRegionAt(int mouseX, int mouseY) {
        return equipmentRegions.stream().filter(region -> region.contains(mouseX, mouseY)).findFirst();
    }

    private int renderEquipmentStorage(GuiGraphics graphics, EquipmentSlot slot, ItemStack equipped, Component emptyTitle, int y,
                                       @Nullable EquipmentEntryHit draggedEquipmentEntry) {
        EquipmentStorageData storage = equipped.get(ModDataComponents.EQUIPMENT_STORAGE.get());
        if (storage == null || storage.containers().isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, emptyTitle, left + 8, y + 4, 0x787878, false);
            return y + 19;
        }
        for (NamedGridInventoryData container : storage.containers()) {
            equipmentRegions.add(new Region(slot, container.id(), container.inventory(), left + 8, y + 13));
            UUID draggedEntryId = draggedEquipmentEntry != null
                    && draggedEquipmentEntry.slot() == slot
                    && draggedEquipmentEntry.containerId().equals(container.id())
                    ? draggedEquipmentEntry.entry().entryId() : null;
            y = renderGrid(graphics, Component.literal(container.title()), container.inventory(), y, draggedEntryId);
        }
        return y;
    }

    private int renderGrid(GuiGraphics graphics, Component title, GridInventoryData inventory, int y, @Nullable UUID draggedEntryId) {
        graphics.drawString(Minecraft.getInstance().font, title, left + 8, y, 0xBFBFBF, false);
        int gridTop = y + 13;
        GridRenderer.renderGrid(graphics, left + 8, gridTop, inventory.getColumns(), inventory.getRows(), CELL);
        inventory.getEntries().forEach(entry -> GridItemRenderer.renderEntry(
                graphics, entry, left + 8, gridTop, CELL,
                draggedEntryId != null && draggedEntryId.equals(entry.entryId()) ? 0.35F : 1.0F
        ));
        return gridTop + inventory.getRows() * CELL + 10;
    }

    private void renderScrollbar(GuiGraphics graphics) {
        if (contentHeight <= height) {
            return;
        }
        int trackTop = top + 20;
        int trackHeight = height - 26;
        int thumbHeight = Math.max(18, trackHeight * height / contentHeight);
        int maxScroll = contentHeight - height;
        int thumbY = trackTop + (trackHeight - thumbHeight) * scroll / maxScroll;
        graphics.fill(left + width - 5, trackTop, left + width - 2, trackTop + trackHeight, 0xFF303030);
        graphics.fill(left + width - 5, thumbY, left + width - 2, thumbY + thumbHeight, 0xFF969696);
    }

    public record EquipmentEntryHit(EquipmentSlot slot, String containerId, GridInventoryData inventory, GridEntry entry, Region region) {
    }

    public record Region(EquipmentSlot slot, String containerId, GridInventoryData inventory, int left, int top) {
        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= left && mouseY >= top
                    && mouseX < left + inventory.getColumns() * CELL
                    && mouseY < top + inventory.getRows() * CELL;
        }

        public int cellX(int mouseX) {
            return (mouseX - left) / CELL;
        }

        public int cellY(int mouseY) {
            return (mouseY - top) / CELL;
        }
    }
}
