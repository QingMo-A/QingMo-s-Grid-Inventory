package com.dreamingfish.gridinventory.client.interaction;

import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.sound.GridInventoryUiSounds;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.network.CreativeDiscardItemMessage;
import com.dreamingfish.gridinventory.common.network.DropCurioMessage;
import com.dreamingfish.gridinventory.common.network.DropEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.DropGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.DropNestedGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.DropPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.MoveItemMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipNestedGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipPlayerSlotMessage;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GridInteractionController {
    private final GridDragSession drag;

    public GridInteractionController(int cellSize) {
        drag = new GridDragSession(cellSize);
    }

    public GridDragSession drag() {
        return drag;
    }

    public void begin(@Nullable GridItemSource source, ItemStack stack, boolean rotated) {
        drag.begin(source, stack, rotated);
    }

    public void begin(@Nullable GridItemSource source, ItemStack stack, boolean rotated, int requestedCount) {
        drag.begin(source, stack, rotated, requestedCount);
    }

    public boolean handleMouseButton(int button) {
        if (button != 1 || !drag.isActive()) {
            return false;
        }
        drag.rotate();
        return true;
    }

    public boolean handlePreviewKey(int keyCode, int scanCode) {
        if (!drag.isActive()) {
            return false;
        }
        if (ModKeyMappings.ROTATE_GRID_ITEM.matches(keyCode, scanCode)) {
            drag.rotate();
            return true;
        }
        if (ModKeyMappings.TOGGLE_BACKPACK_FOLD.matches(keyCode, scanCode)) {
            if (drag.toggleFolded()) {
                GridInventoryUiSounds.foldBackpack();
            }
            return true;
        }
        return false;
    }

    public ReleaseResult release(int mouseX, int mouseY, List<GridInteractionSurface> surfaces) {
        if (!drag.isActive() || drag.source().isEmpty()) {
            return ReleaseResult.missed();
        }
        for (GridInteractionSurface surface : surfaces) {
            GridInteractionSurface.DropHit hit = surface.resolveDrop(drag, mouseX, mouseY);
            if (hit.kind() == GridInteractionSurface.Kind.PASS) {
                continue;
            }
            if (hit.kind() == GridInteractionSurface.Kind.BLOCKED) {
                return ReleaseResult.blocked();
            }
            GridItemTarget target = hit.target();
            if (target == null || !GridClientMoveRules.supports(drag.source().orElseThrow(), target)) {
                return ReleaseResult.blocked();
            }
            sendMove(drag.source().orElseThrow(), target,
                    hit.options() == null ? drag.options() : hit.options());
            return ReleaseResult.sent(target);
        }
        return ReleaseResult.missed();
    }

    public boolean discardCreative() {
        return drag.source().map(source -> {
            GridInventoryServices.network().sendToServer(new CreativeDiscardItemMessage(source));
            return true;
        }).orElse(false);
    }

    public boolean quickEquip(GridItemSource source) {
        if (source instanceof GridItemSource.MenuGridEntry entry) {
            GridInventoryServices.network().sendToServer(new QuickEquipGridEntryMessage(entry.entryId()));
        } else if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
            GridInventoryServices.network().sendToServer(new QuickEquipEquipmentStorageEntryMessage(
                    entry.slot(), entry.containerId(), entry.entryId()));
        } else if (source instanceof GridItemSource.NestedGridEntry entry) {
            GridInventoryServices.network().sendToServer(new QuickEquipNestedGridEntryMessage(
                    entry.ownerPath(), "", entry.entryId()));
        } else if (source instanceof GridItemSource.NestedEquipmentStorageEntry entry) {
            GridInventoryServices.network().sendToServer(new QuickEquipNestedGridEntryMessage(
                    entry.ownerPath(), entry.containerId(), entry.entryId()));
        } else if (source instanceof GridItemSource.PlayerSlot slot) {
            GridInventoryServices.network().sendToServer(new QuickEquipPlayerSlotMessage(slot.slot()));
        } else {
            return false;
        }
        GridInventoryUiSounds.equip();
        return true;
    }

    public boolean drop(GridItemSource source) {
        if (source instanceof GridItemSource.MenuGridEntry entry) {
            GridInventoryServices.network().sendToServer(new DropGridEntryMessage(entry.entryId()));
        } else if (source instanceof GridItemSource.EquipmentStorageEntry entry) {
            GridInventoryServices.network().sendToServer(new DropEquipmentStorageEntryMessage(
                    entry.slot(), entry.containerId(), entry.entryId()));
        } else if (source instanceof GridItemSource.NestedGridEntry entry) {
            GridInventoryServices.network().sendToServer(new DropNestedGridEntryMessage(
                    entry.ownerPath(), "", entry.entryId()));
        } else if (source instanceof GridItemSource.NestedEquipmentStorageEntry entry) {
            GridInventoryServices.network().sendToServer(new DropNestedGridEntryMessage(
                    entry.ownerPath(), entry.containerId(), entry.entryId()));
        } else if (source instanceof GridItemSource.AccessorySlot slot) {
            GridInventoryServices.network().sendToServer(new DropCurioMessage(slot.identifier(), slot.index()));
        } else if (source instanceof GridItemSource.PlayerSlot slot) {
            GridInventoryServices.network().sendToServer(new DropPlayerSlotMessage(slot.slot()));
        } else {
            return false;
        }
        return true;
    }

    public void sendMove(GridItemSource source, GridItemTarget target, GridMoveOptions options) {
        GridInventoryServices.network().sendToServer(new MoveItemMessage(source, target, options));
    }

    public void clear() {
        drag.clear();
    }

    public record ReleaseResult(boolean handled, boolean sent, @Nullable GridItemTarget target) {
        public static ReleaseResult missed() {
            return new ReleaseResult(false, false, null);
        }

        public static ReleaseResult blocked() {
            return new ReleaseResult(true, false, null);
        }

        public static ReleaseResult sent(GridItemTarget target) {
            return new ReleaseResult(true, true, target);
        }
    }
}
