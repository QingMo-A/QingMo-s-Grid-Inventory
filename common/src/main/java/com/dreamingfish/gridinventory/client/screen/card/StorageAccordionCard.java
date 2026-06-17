package com.dreamingfish.gridinventory.client.screen.card;

import com.dreamingfish.gridinventory.client.screen.panel.GridColumnPanel;
import com.dreamingfish.gridinventory.client.ui.animation.HoverAnimationTracker;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.UUID;

public final class StorageAccordionCard {
    private static final int GAP = 8;

    private final StorageAccordionState state;

    public StorageAccordionCard(StorageAccordionState state) {
        this.state = state;
    }

    public int render(GuiGraphics graphics, StorageCardData data, int x, int y, int width, int mouseX, int mouseY,
                      @Nullable UUID draggedPocketEntryId,
                      @Nullable GridColumnPanel.EquipmentEntryHit draggedEquipmentEntry,
                      HoverAnimationTracker<UUID> pocketHoverAnimations,
                      HoverAnimationTracker<String> equipmentHoverAnimations,
                      boolean hoverEnabled,
                      List<GridColumnPanel.Region> equipmentRegions) {
        boolean hovered = containsHeader(x, y, width, mouseX, mouseY);
        state.update(hovered, data.available());
        StorageCardHeaderRenderer.render(graphics, data, x, y, width, state.buttonFrame());
        int bodyFullHeight = StorageCardBodyRenderer.fullHeight(data);
        int bodyHeight = Math.round(bodyFullHeight * state.expansionProgress());
        StorageCardBodyRenderer.render(graphics, data, x, y + StorageCardHeaderRenderer.HEIGHT, width, bodyHeight,
                state.expansionProgress(), mouseX, mouseY,
                draggedPocketEntryId, draggedEquipmentEntry, pocketHoverAnimations, equipmentHoverAnimations,
                hoverEnabled, equipmentRegions);
        return StorageCardHeaderRenderer.HEIGHT + bodyHeight + GAP;
    }

    public boolean mouseClicked(StorageCardData data, int x, int y, int width, double mouseX, double mouseY, int button) {
        if (button != 0 || !containsHeader(x, y, width, mouseX, mouseY)) {
            return false;
        }
        return state.toggle(data.available());
    }

    public int animatedHeight(StorageCardData data) {
        return StorageCardHeaderRenderer.HEIGHT
                + Math.round(StorageCardBodyRenderer.fullHeight(data) * state.expansionProgress()) + GAP;
    }

    public boolean expanded() {
        return state.expanded();
    }

    private static boolean containsHeader(int x, int y, int width, double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + StorageCardHeaderRenderer.HEIGHT;
    }
}
