package com.dreamingfish.gridinventory.client.screen.panel;

import com.dreamingfish.gridinventory.client.render.GridItemRenderer;
import com.dreamingfish.gridinventory.client.render.GridLayoutMetrics;
import com.dreamingfish.gridinventory.client.screen.card.StorageAccordionCard;
import com.dreamingfish.gridinventory.client.screen.card.StorageAccordionState;
import com.dreamingfish.gridinventory.client.screen.card.StorageCardData;
import com.dreamingfish.gridinventory.client.screen.card.StorageCardHeaderRenderer;
import com.dreamingfish.gridinventory.client.screen.card.StorageCardKey;
import com.dreamingfish.gridinventory.client.ui.animation.HoverAnimationTracker;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GridColumnPanel {
    private static final int CELL = 27;
    private int left;
    private int top;
    private int width;
    private int height;
    private int scroll;
    private int contentHeight;
    private final PanelScrollbar scrollbar = new PanelScrollbar();
    private final HoverAnimationTracker<String> equipmentHoverAnimations = new HoverAnimationTracker<>();
    private final Map<String, StorageAccordionState> cardStates = new HashMap<>();
    private final List<CardLayout> cardLayouts = new ArrayList<>();
    private final List<Region> equipmentRegions = new ArrayList<>();

    public void setBounds(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public int pocketLeft() {
        return left + 18;
    }

    public int pocketTop() {
        return top + 22 + StorageCardHeaderRenderer.HEIGHT + 8 - scroll;
    }

    public void render(GuiGraphics graphics, GridInventoryData pocket, @Nullable UUID draggedPocketEntryId,
                       @Nullable EquipmentEntryHit draggedEquipmentEntry, int mouseX, int mouseY,
                       HoverAnimationTracker<UUID> pocketHoverAnimations, boolean hoverEnabled) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.fill(left, top, left + width, top + height, 0x2E1A1A1A);
        graphics.drawString(minecraft.font, Component.translatable("screen.df_grid_inventory.storage"), left + 8, top + 6, 0xFFFFFF, false);
        scrollbar.update(left, top + 18, width - 6, height - 22, contentHeight);
        scroll = scrollbar.scroll();
        graphics.enableScissor(left, top + 18, left + width - 6, top + height - 4);
        equipmentRegions.clear();
        cardLayouts.clear();
        List<StorageCardData> cards = collectCards(pocket);
        int y = top + 22 - scroll;
        int cardWidth = Math.max(80, width - 28);
        for (StorageCardData cardData : cards) {
            StorageAccordionCard card = card(cardData.stateKey());
            cardLayouts.add(new CardLayout(cardData, left + 8, y, cardWidth));
            y += card.render(graphics, cardData, left + 8, y, cardWidth, mouseX, mouseY,
                    draggedPocketEntryId, draggedEquipmentEntry, pocketHoverAnimations, equipmentHoverAnimations,
                    hoverEnabled, equipmentRegions);
        }
        graphics.disableScissor();
        contentHeight = Math.max(height, y - top + scroll + 6);
        scrollbar.update(left, top + 18, width - 6, height - 22, contentHeight);
        renderScrollbar(graphics, mouseX, mouseY);
        equipmentHoverAnimations.markFrameEnd();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        return scrollbar.mouseScrolled(mouseX, mouseY, deltaY, CELL);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CardLayout layout : cardLayouts) {
            if (card(layout.data().stateKey()).mouseClicked(layout.data(), layout.x(), layout.y(), layout.width(), mouseX, mouseY, button)) {
                return true;
            }
        }
        return scrollbar.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return scrollbar.mouseDragged(mouseX, mouseY, button);
    }

    public boolean mouseReleased(int button) {
        return scrollbar.mouseReleased(button);
    }

    public Optional<EquipmentEntryHit> equipmentEntryAt(int mouseX, int mouseY) {
        return equipmentRegions.stream()
                .filter(region -> region.contains(mouseX, mouseY))
                .flatMap(region -> region.inventory().getEntries().stream()
                        .filter(entry -> entry.contains(region.cellX(mouseX, mouseY), region.cellY(mouseX, mouseY)))
                        .map(entry -> new EquipmentEntryHit(region.slot(), region.containerId(), region.inventory(), entry, region)))
                .findFirst();
    }

    public Optional<Region> equipmentRegionAt(int mouseX, int mouseY) {
        return equipmentRegions.stream().filter(region -> region.contains(mouseX, mouseY)).findFirst();
    }

    private List<StorageCardData> collectCards(GridInventoryData pocket) {
        List<StorageCardData> cards = new ArrayList<>();
        cards.add(new StorageCardData(StorageCardKey.POCKET,
                "pocket",
                Component.translatable("screen.df_grid_inventory.pocket"),
                storageSubtitle(List.of(new NamedGridInventoryData("pocket", "pocket", pocket))),
                ItemStack.EMPTY, true, null, List.of(new NamedGridInventoryData("pocket", "pocket", pocket))));
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return cards;
        }
        addEquipmentCard(cards, "chest", StorageCardKey.CHEST, EquipmentSlot.CHEST, minecraft.player.getItemBySlot(EquipmentSlot.CHEST));
        addEquipmentCard(cards, "legs", StorageCardKey.LEGS, EquipmentSlot.LEGS, minecraft.player.getItemBySlot(EquipmentSlot.LEGS));
        if (GridInventoryServices.accessories().isLoaded()) {
            GridInventoryServices.accessories().collectSlots(minecraft.player).stream()
                    .filter(slot -> "back".equals(slot.identifier()))
                    .filter(slot -> !slot.stack().isEmpty())
                    .forEach(slot -> addEquipmentCard(cards, "backpack:" + slot.identifier() + ":" + slot.index(),
                            StorageCardKey.BACKPACK, GridEquipmentSlots.back(), slot.stack()));
        }
        return cards;
    }

    private void addEquipmentCard(List<StorageCardData> cards, String stateKey, StorageCardKey key, EquipmentSlot slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        EquipmentStorageData storage = EquipmentStorageManager.initializeStorage(stack, slot);
        List<NamedGridInventoryData> containers = storage == null ? List.of() : storage.containers();
        if (containers.isEmpty()) {
            return;
        }
        cards.add(new StorageCardData(key, stateKey, stack.getHoverName(),
                containers.isEmpty() ? Component.translatable("screen.df_grid_inventory.no_storage_space")
                        : storageSubtitle(containers),
                stack, true, slot, containers));
    }

    private Component storageSubtitle(List<NamedGridInventoryData> containers) {
        if (containers.size() > 1) {
            return Component.translatable("screen.df_grid_inventory.storage_regions", containers.size());
        }
        if (containers.isEmpty()) {
            return Component.translatable("screen.df_grid_inventory.no_storage_space");
        }
        return Component.empty();
    }

    private StorageAccordionCard card(String key) {
        return new StorageAccordionCard(cardStates.computeIfAbsent(key, ignored -> new StorageAccordionState()));
    }

    private void renderScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        scrollbar.render(graphics, mouseX, mouseY);
    }

    public record EquipmentEntryHit(EquipmentSlot slot, String containerId, GridInventoryData inventory, GridEntry entry, Region region) {
    }

    private record CardLayout(StorageCardData data, int x, int y, int width) {
    }

    public record Region(EquipmentSlot slot, String containerId, GridInventoryData inventory, int left, int top) {
        private boolean contains(int mouseX, int mouseY) {
            int cellX = cellX(mouseX, mouseY);
            int cellY = cellY(mouseX, mouseY);
            return mouseX >= left && mouseY >= top
                    && mouseX < left + GridLayoutMetrics.width(inventory, CELL)
                    && mouseY < top + GridLayoutMetrics.height(inventory, CELL)
                    && cellX >= 0 && cellY >= 0
                    && inventory.isEnabledCell(cellX, cellY);
        }

        public int cellX(int mouseX) {
            return GridLayoutMetrics.cellXAt(inventory, mouseX - left, 0, CELL);
        }

        public int cellY(int mouseY) {
            return GridLayoutMetrics.cellYAt(inventory, 0, mouseY - top, CELL);
        }

        public int cellX(int mouseX, int mouseY) {
            return GridLayoutMetrics.cellXAt(inventory, mouseX - left, mouseY - top, CELL);
        }

        public int cellY(int mouseX, int mouseY) {
            return GridLayoutMetrics.cellYAt(inventory, mouseX - left, mouseY - top, CELL);
        }

        public int drawX(int cellX) {
            return drawX(cellX, 0);
        }

        public int drawY(int cellY) {
            return drawY(0, cellY);
        }

        public int drawX(int cellX, int cellY) {
            return left + GridLayoutMetrics.cellLeft(inventory, cellX, cellY, CELL);
        }

        public int drawY(int cellX, int cellY) {
            return top + GridLayoutMetrics.cellTop(inventory, cellX, cellY, CELL);
        }

        public int areaWidth(int cellX, int width) {
            return areaWidth(cellX, 0, width);
        }

        public int areaHeight(int cellY, int height) {
            return areaHeight(0, cellY, height);
        }

        public int areaWidth(int cellX, int cellY, int width) {
            return GridLayoutMetrics.areaWidth(inventory, cellX, cellY, width, CELL);
        }

        public int areaHeight(int cellX, int cellY, int height) {
            return GridLayoutMetrics.areaHeight(inventory, cellX, cellY, height, CELL);
        }
    }
}
